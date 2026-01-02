// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Resource Types', () => {
    test.beforeEach(async ({ page }) => {
        // Login
        await page.goto('/login');
        await page.fill('input[type="text"]', 'admin');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/');
        // Navigate to Resources
        await page.click('a[href="/definitions/resources"]');
    });

    test('should create a new resource type', async ({ page }) => {
        await page.click('button:has-text("New Resource Type")'); // Adjust selector as needed

        // Fill Form
        // Assuming form inside a modal or slide-over
        await page.fill('input[name="name"]', 'e2e-resource');
        await page.fill('textarea[name="description"]', 'Created by E2E test');

        // Add Actions (assuming tag input or similar)
        const actionsInput = page.locator('input[placeholder="Add action..."]');
        await actionsInput.fill('read');
        await actionsInput.press('Enter');
        await actionsInput.fill('write');
        await actionsInput.press('Enter');

        await page.click('button:has-text("Save")');

        // Verify
        await expect(page.locator('text=e2e-resource')).toBeVisible();
        await expect(page.locator('text=read')).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
        await page.click('button:has-text("New Resource Type")');
        await page.click('button:has-text("Save")'); // Submit empty

        // Verify error messages
        await expect(page.locator('text=Name is required')).toBeVisible(); // Adjust expected text
    });
});
