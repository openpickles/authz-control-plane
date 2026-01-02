// @ts-check
import { test, expect } from '@playwright/test';

test.describe('User Management', () => {
    test.beforeEach(async ({ page }) => {
        // Login
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');
        // Navigate to Users
        await page.click('a[href="/system/users"]');
    });

    test('should create and delete a user', async ({ page }) => {
        // Ensure on Users tab
        await page.click('button:has-text("Users")');

        // Open Modal
        await page.click('button:has-text("Add User")');

        // Fill Form (User creation might rely on just Name in this mock impl? 
        // Checking UserManagement.jsx: creates user with just username)
        await page.fill('input[type="text"]', 'e2e-user');
        await page.click('button[type="submit"]'); // "Create" button

        // Verify User Listed
        await expect(page.locator('text=e2e-user')).toBeVisible();

        // Delete User
        page.on('dialog', dialog => dialog.accept()); // Handle confirm dialog
        // Find the row with e2e-user and click delete
        const userRow = page.locator('.card', { hasText: 'e2e-user' });
        await userRow.locator('button').click();

        // Verify Deleted
        await expect(page.locator('text=e2e-user')).not.toBeVisible();
    });

    test('should create a role', async ({ page }) => {
        await page.click('button:has-text("Roles")');
        await page.click('button:has-text("Add Role")');
        await page.fill('input[type="text"]', 'e2e-role');
        await page.click('button[type="submit"]');

        await expect(page.locator('text=e2e-role')).toBeVisible();
    });

    test('should create a group', async ({ page }) => {
        await page.click('button:has-text("Groups")');
        await page.click('button:has-text("Add Group")');
        await page.fill('input[type="text"]', 'e2e-group');
        await page.click('button[type="submit"]');

        await expect(page.locator('text=e2e-group')).toBeVisible();
    });
});
