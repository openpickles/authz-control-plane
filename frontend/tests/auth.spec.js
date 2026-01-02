// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
    test('should allow login with valid credentials', async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');

        await expect(page).toHaveURL('/');
        await expect(page.locator('text=Policy Engine')).toBeVisible();
    });

    test('should show error with invalid credentials', async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', 'wrong');
        await page.fill('input[type="password"]', 'wrong');
        await page.click('button[type="submit"]');

        await expect(page.locator('text=Invalid credentials')).toBeVisible();
    });

    test('should allow logout', async ({ page }) => {
        // Login first
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');

        // Logout (Find Logout button in sidebar or header)
        // Assuming it's in the sidebar
        await page.click('text=Logout');
        await expect(page).toHaveURL('/login');
    });
});
