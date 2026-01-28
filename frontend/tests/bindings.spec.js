// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bindings', () => {
    test.beforeEach(async ({ page }) => {
        const uniqueId = Date.now();
        const policyName = `e2e-test-policy-${uniqueId}`;

        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');

        // Create dependency Policy using session
        // Check Policy Creation
        const response = await page.request.post('/api/v1/policies', {
            data: {
                name: policyName,
                content: 'package test\ndefault allow = true',
                filename: `e2e-test-${uniqueId}.rego`
            }
        });
        expect(response.status()).toBe(200);

        // Store policy name for the test
        process.env.TEST_POLICY_NAME = policyName;

        await page.click('a[aria-label="Policy Engine"]');
        await page.click('a[href="/policy-bindings"]');
    });

    test('should create a binding', async ({ page }) => {
        const policyName = process.env.TEST_POLICY_NAME;
        const uniqueId = Date.now();
        const resourceType = `test-resource-type-${uniqueId}`;
        const context = `e2e-context-pbc-${uniqueId}`;

        await page.click('button:has-text("Create Binding")');

        // Select Resource Type (Custom to ensure independence)
        await page.selectOption('select#resourceType', 'custom');
        await page.fill('input[placeholder="e.g. widget"]', resourceType);

        // Fill Context
        await page.fill('input[placeholder="e.g., fine_grained_access"]', context);

        // Select Policy (Required by backend)
        await page.click('text=Select policies...');
        // Use exact text matching if possible or ensure it's loaded
        await expect(page.locator(`text=${policyName}`)).toBeVisible();
        await page.click(`text=${policyName}`);

        // Close dropdown by clicking outside (e.g. title)
        await page.click('text=Create Policy Binding');

        // Select Evaluation Mode (PBC_CHAIN)
        await page.locator('label:has-text("Evaluation Mode") + select').selectOption('PBC_CHAIN');

        // Check if Policy ID is actually selected (Pill visible)
        // MultiSelect pill has class that includes 'bg-brand-50'
        await expect(page.locator(`span.bg-brand-50:has-text("${policyName}")`)).toBeVisible();

        // Click the submit button inside the form (modal)
        // Using force click to ensure it works even if animating
        const [createResponse] = await Promise.all([
            page.waitForResponse(res => res.url().includes('/api/v1/policy-bindings') && res.request().method() === 'POST'),
            page.click('form button[type="submit"]', { force: true })
        ]);

        expect(createResponse.status()).toBe(200);

        // Check if slideover closed (meaning success)
        await expect(page.locator('text=Create Policy Binding')).not.toBeVisible();

        // Validation: Verify it appears in the list
        await expect(page.locator('tbody')).toContainText('PBC_CHAIN');
    });
});
