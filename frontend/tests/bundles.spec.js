// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bundles', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');
        await page.click('a[href="/policies/bundles"]');
    });

    test('should create and build a bundle', async ({ page }) => {
        // Navigate to Bundles
        await page.click('button:has-text("Create Bundle")');

        await page.fill('input[placeholder="Bundle Name"]', 'e2e-bundle');
        // Select policies (assuming a multi-select or list)
        // We need to click "Select Policies" likely
        // This depends on PolicyBundles.jsx logic.
        // Assuming simple flow for now:
        await page.click('button:has-text("Save")');

        // Build
        await page.click('button[title="Build Bundle"]');

        // Expect success toast or status change
        await expect(page.locator('text=Build triggered')).toBeVisible();
    });
});
