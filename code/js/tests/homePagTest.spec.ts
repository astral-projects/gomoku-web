import { test, expect } from '@playwright/test';

export const port = 4000;
export const BASE = `http://localhost:${port}/`;

test('has title', async ({ page }) => {
    await page.goto(BASE);

    // Expect a title "to contain" a substring.
    await expect(page).toHaveTitle('Gomoku Royale');
});

// test the home page
test('home page', async ({ page }) => {
    await page.goto(BASE);

    // Expect the page to be loaded.
    await expect(page).toHaveURL(`${BASE}`);

    // Expect that home page contains the text "Welcome to Gomoku Royale".
    // await expect(page.getByText('Welcome to Gomoku Royale!')).toBeTruthy();
});

test('clicking in login of home page goes to login page', async ({ page }) => {
    await page.goto(BASE);

    await page.click('text=Login');

    await expect(page).toHaveURL(`${BASE}login`);
});

test('clicking in register of home page goes to register page', async ({ page }) => {
    await page.goto(BASE);

    await page.click('text=Sign Up');

    await expect(page).toHaveURL(`${BASE}register`);
});

test('clicking in About of navbar in home page goes to about page', async ({ page }) => {
    await page.goto(BASE);

    await page.click('text=About');

    await expect(page).toHaveURL(`${BASE}about`);
});
