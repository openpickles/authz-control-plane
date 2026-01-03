// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Resource Types', () => {
    test.beforeEach(async ({ page }) => {
        // Login
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');
        // Debug Logging
        page.on('console', msg => console.log('BROWSER LOG:', msg.text()));
        page.on('response', response => {
            console.log('API RESPONSE:', response.url(), response.status());
        });

        // Navigate to Resources
        await page.click('text=Definition'); // Primary rail
        await page.click('a[href="/resource-types"]');
    });

    test('should create a new resource type', async ({ page }) => {
        await page.click('button:has-text("Register Type")'); // Match actual UI

        // Fill Form
        // Assuming form inside a modal or slide-over
        // Fill Form
        await page.fill('input[id="displayName"]', 'e2e-resource');
        await page.fill('input[id="uniqueKey"]', 'e2e:resource');
        await page.fill('textarea[id="description"]', 'Created by E2E test');
        await page.fill('input[id="baseUrl"]', 'http://example.com');

        await page.click('form button:has-text("Register Type")');

        // Verify
        await expect(page.locator('text=e2e-resource')).toBeVisible();
        await expect(page.locator('text=read')).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
        await page.click('button:has-text("Register Type")');
        await page.click('form button:has-text("Register Type")'); // Submit empty

        // Verify error messages
        // Verify error messages - assuming browser validation or specific messages.
        // For now check if we are still on the form or if an error appeared.
        // Or check validation attribute if playwright supports it, or just absence of success.
        await expect(page.locator('button:has-text("Register Type")').nth(1)).toBeVisible(); // Should still be visible

    });
});
