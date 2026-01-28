import { test, expect } from '@playwright/test';

test.describe('Distributed Policy Sync Lifecycle', () => {

    const serviceName = 'e2e-test-service-' + Math.random().toString(36).substring(7);

    test.beforeEach(async ({ page, request }) => {
        // 1. Simulate "Push" from a service (Bootstrap)
        // We use the API directly to register the service and policies
        const manifestPayload = {
            manifest: {
                service: {
                    name: serviceName,
                    version: '1.0.0',
                    description: 'E2E Test Service'
                },
                policies: [
                    {
                        name: 'e2e-policy.rego',
                        file: 'e2e-policy.rego',
                        content: 'package e2e\ndefault allow = false',
                        version: 'v1'
                    }
                ],
                resourceTypes: [
                    {
                        name: 'api-resource',
                        key: 'api', // key is used in binding
                        description: 'API Resource'
                    }
                ],
                bindings: [
                    {
                        resourceType: 'api',
                        context: 'e2e-context',
                        policies: ['e2e-policy.rego']
                    }
                ],
                bundles: [
                    {
                        name: 'e2e-bundle',
                        targetService: 'e2e-target',
                        refreshInterval: '10s',
                        contexts: ['e2e-context']
                    }
                ]
            },
            manifestHash: 'hash-' + Date.now()
        };

        const response = await request.post('http://localhost:8080/api/v1/dist/sync', {
            data: manifestPayload
        });
        if (!response.ok()) {
            console.log('Sync Request Failed:', response.status(), await response.text());
        }
        expect(response.ok()).toBeTruthy();

        // Login to UI
        await page.goto('http://localhost:5173/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || 'admin');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || 'admin123');
        await page.click('button[type="submit"]');
        // Wait for navigation or dashboard element
        await expect(page).toHaveURL(/\/$/);
        await expect(page.locator('text=System Overview')).toBeVisible(); // Dashboard header
    });

    test('should manage service policies and verify effective bundle', async ({ page, request }) => {
        // 2. Navigate to Services List
        await page.click('a[href="/services"]');
        await expect(page.locator(`text=${serviceName}`)).toBeVisible();

        // 3. Drill down to Service Detail
        await page.click(`a[href="/services/${serviceName}"]`);
        await expect(page.locator(`h1:has-text("Service: ${serviceName}")`)).toBeVisible();

        // 4. Verify Policy exists
        const policyRow = page.locator('tr').filter({ hasText: 'e2e-policy.rego' });
        await expect(policyRow).toBeVisible();
        await expect(policyRow).toContainText('PRODUCT');

        // 5. Customize Policy
        await policyRow.getByRole('button', { name: 'Customize / Override' }).click();

        await expect(page.getByText('Customize: e2e-policy.rego')).toBeVisible();

        // Edit content
        await page.locator('textarea').fill('package e2e\ndefault allow = true');
        await page.getByRole('button', { name: 'Save Override' }).click();

        // Wait for processing
        await page.waitForTimeout(500);

        // Wait for slide over to close and refresh (UI might not auto-refresh fast enough, so we refresh manually if needed, but app does fetchData)
        // The test waits for the button to disappear or status to update?
        // Let's reload to be sure or verify UI state
        await page.reload();
        await expect(page.locator('text=Edit Customization')).toBeVisible(); // Should change button text

        // 6. Verify "Pull" (Effective Bundle)
        // Download bundle via API using the correct name-based endpoint
        // URL: /api/v1/bundles/{name}/download?service={serviceName}
        const bundleResponse = await request.get(`http://localhost:8080/api/v1/bundles/e2e-bundle/download?service=${serviceName}`);

        if (!bundleResponse.ok()) {
            console.log('Bundle Download Failed:', bundleResponse.status(), await bundleResponse.text());
        }
        expect(bundleResponse.ok()).toBeTruthy();

        // We can't easily check gzip content in Playwright without libraries, 
        // but we can check if it returns 200.
        // To verify the CONTENT, we'd need to unzip.
        // For E2E, verifying status 200 and maybe headers is good start.
        const contentType = bundleResponse.headers()['content-type'];
        expect(contentType).toBe('application/gzip');

        // Optional: We can try to fetch the policy content via Admin API to check if CUSTOM override exists
        const policyRes = await request.get(`http://localhost:8080/api/v1/policies/policies?service=${serviceName}&origin=CUSTOM`);
        const policyData = await policyRes.json();
        const customPolicy = policyData.content.find(p => p.name === 'e2e-policy.rego');
        expect(customPolicy).toBeDefined();
        expect(customPolicy.content).toContain('default allow = true');
    });
});
