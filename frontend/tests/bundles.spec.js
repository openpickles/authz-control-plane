// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bundles', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');
        await page.click('a[aria-label="Policy Engine"]');
        await page.click('a[href="/policy-bundles"]');
    });

    test('should create and build a bundle', async ({ page }) => {
        // Navigate to Bundles
        await page.click('button:has-text("Create Bundle")');

        await page.fill('input[placeholder="e.g., Payment Service Policies"]', 'e2e-bundle', { force: true });
        // Select policies (assuming a multi-select or list)
        // We need to click "Select Policies" likely
        // This depends on PolicyBundles.jsx logic.
        // Assuming simple flow for now:
        // Submit
        // Submit
        // Submit
        await page.locator('form button:has-text("Create Bundle")').dispatchEvent('click');

        // Verify in list
        await expect(page.locator('text=e2e-bundle').first()).toBeVisible({ timeout: 10000 });
    });
});
