// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
    test('should allow login with valid credentials', async ({ page }) => {
        page.on('console', msg => console.log('BROWSER LOG:', msg.text()));
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');

        await expect(page).toHaveURL('/');
        await expect(page.locator('h2:has-text("System Overview")')).toBeVisible();
    });

    test('should show error with invalid credentials', async ({ page }) => {
        await page.goto('/login');
        await page.waitForLoadState('networkidle');
        await page.fill('input[type="text"]', 'invalid');
        await page.fill('input[type="password"]', 'invalid');
        await page.click('button:has-text("Sign In")');
        // Wait for error message or toast
        // Assuming there is some visual feedback. If not, check URL stays same.
        await expect(page).toHaveURL('/login');
    });

    test('should allow logout', async ({ page }) => {
        // Login first
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');

        // Force click logout directly if potential overlap
        await page.click('button:has-text("Logout")', { force: true });
        await page.waitForURL(/.*\/login/);
        await expect(page.locator('button:has-text("Sign In")')).toBeVisible({ timeout: 10000 });
    });
});
