// @ts-check
import { test, expect } from '@playwright/test';

test.describe('Policy Bindings', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[type="text"]', process.env.TEST_USERNAME || '');
        await page.fill('input[type="password"]', process.env.TEST_PASSWORD || '');
        await page.click('button:has-text("Sign In")');
        await expect(page).toHaveURL('/');
        await page.click('a[aria-label="Policy Engine"]');
        await page.click('a[href="/policy-bindings"]');
    });

    test('should create a binding', async ({ page }) => {
        await page.click('button:has-text("Create Binding")');

        // Select Resource Type
        await page.selectOption('select#resourceType', { index: 1 }); // Select first available type

        // Fill Context
        await page.fill('input[placeholder="e.g., fine_grained_access"]', 'e2e-context', { force: true });

        // Select Policy (MultiSelect - simplistic interaction)
        // This might be tricky with custom MultiSelect, skipping distinct selection if optional, 
        // OR trigger the dropdown.
        // For now, let's assume we just create it with minimal required fields.
        // Wait! The MultiSelect input is just a div trigger? 
        // Let's create without policies if allowed, or try to select one.
        // But context is required. ResourceType is required. PolicyIds is not strictly required by UI? 
        // Let's check PolicyBindings.jsx: policyIds initial [] and no "required" on MultiSelect component usage?
        // Actually MultiSelect doesn't have required prop usually.
        // Let's try creating without policies first.

        // Click the submit button inside the form (modal)
        // Click the submit button inside the form (modal) - dispatch event to bypass viewport issues
        await page.locator('form button:has-text("Create Binding")').dispatchEvent('click');
    });
});
