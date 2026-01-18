// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bundles', () => {
    const serviceName = 'bundle-test-service-' + Math.random().toString(36).substring(7);

    test.beforeEach(async ({ page, request }) => {
        // Create a service first so we can select it
        await request.post('http://localhost:8080/api/v1/services', {
            data: {
                name: serviceName,
                description: 'Service for Bundle Test',
                version: '1.0.0',
                registrationMode: 'MANUAL',
                publicKey: ''
            }
        });

        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || 'admin');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || 'admin123');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');
        await page.click('a[href="/policy-bundles"]');
    });

    test('should create and build a bundle', async ({ page }) => {
        // Navigate to Bundles
        // Use getByRole which is more robust
        await page.getByRole('button', { name: 'Create Bundle' }).first().click();

        await page.fill('input[placeholder="e.g., Payment Service Policies"]', 'e2e-bundle', { force: true });

        // Select Service Owner
        await page.selectOption('select', { label: `${serviceName} (MANUAL)` });

        // Submit button in form
        // Using form selector to be safe
        await page.click('form button[type="submit"]', { force: true });

        // Verify in list
        await expect(page.locator('text=e2e-bundle').first()).toBeVisible({ timeout: 10000 });
    });
});
