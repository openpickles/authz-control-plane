// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bindings', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');
        await page.click('a[href="/definitions/bindings"]');
    });

    test('should create a binding', async ({ page }) => {
        await page.click('button:has-text("Create Binding")');

        await page.fill('input[name="name"]', 'e2e-binding'); // Adjust selector
        // Select Policy/Bundle
        // Select Target

        await page.click('button:has-text("Save")');

        await expect(page.locator('text=e2e-binding')).toBeVisible();
    });
});
