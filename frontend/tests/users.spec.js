// @ts-check
import { test, expect } from '@playwright/test';

test.describe('User Management', () => {
    test.beforeEach(async ({ page }) => {
        // Login
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');
        // Navigate to Users
        await page.click('text=Settings'); // Expand 'Administration' in primary rail (or logic might be implied)
        // Wait for secondary rail expantion or if it is already expanded
        await page.click('a[href="/users"]');
    });

    test('should create and delete a user', async ({ page }) => {
        // Ensure on Users tab
        await page.click('button:has-text("Users")');

        // Open Modal
        await page.click('button:has-text("Add user")'); // Lowercase 'user' based on activeTab.slice

        // Fill Form
        await page.fill('.fixed.inset-0 input[type="text"]', 'e2e-user');
        await page.click('button:has-text("Create")'); // "Create" button

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
        await page.click('button:has-text("Add role")');
        await page.fill('.fixed.inset-0 input[type="text"]', 'e2e-role');
        await page.click('button:has-text("Create")');

        await expect(page.locator('text=e2e-role')).toBeVisible();
    });

    test('should create a group', async ({ page }) => {
        await page.click('button:has-text("Groups")');
        await page.click('button:has-text("Add group")');
        await page.fill('.fixed.inset-0 input[type="text"]', 'e2e-group');
        await page.click('button:has-text("Create")');

        await expect(page.locator('text=e2e-group')).toBeVisible();
    });
});
