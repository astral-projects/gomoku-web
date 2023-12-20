import { expect, test } from '@playwright/test';
import { generateRandomEmail, generateRandomPassword, generateRandomUsername } from './utils/generateRandomUser';

export const port = 4000;
export const BASE = `http://localhost:${port}/`;

const username = generateRandomUsername();
const email = generateRandomEmail();
const password = generateRandomPassword();

test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE}register`);
    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Email').fill(email);
    await page.locator('input[name="password"]').fill(password);
    await page.locator('input[name="confirmPassword"]').fill(password);
    await page.getByRole('button', { name: 'Sign Up' }).click();

    await page.waitForTimeout(1000);

    await page.goto(`${BASE}login`);

    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Password').fill(password);
    await page.getByRole('button', { name: 'Login' }).click();

    // do a delay to wait for the page to load
    await page.waitForTimeout(1000);

    await page.goto(`${BASE}me`);
});

test('has greeting the user', async ({ page }) => {
    await expect(page.locator('text=Hello, ' + username + '!')).toBeTruthy();
});

test('clicking in logout button goes to home page', async ({ page }) => {
    await page.locator('text=Logout').click();

    await page.waitForTimeout(1000);

    await expect(page).toHaveURL(`${BASE}`);
});

test('clicking in Find Match goes to variants page', async ({ page }) => {
    await page.locator('text=Find Match').click();

    await page.waitForTimeout(1000);

    await expect(page).toHaveURL(`${BASE}games`);
});
