import { chromium, expect, firefox, test } from '@playwright/test';
import { generateRandomEmail, generateRandomPassword, generateRandomUsername } from './utils/generateRandomUser';
import { text } from 'stream/consumers';

export const port = 4000;
export const BASE = `http://localhost:${port}/`;

const username = generateRandomUsername();
const email = generateRandomEmail();
const password = generateRandomPassword();

// Giving: a logged in user
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

    await page.goto(`${BASE}games`);
});

// When: the page of variants is loaded
// Then: the page should have a selector with the text "-- select a variant --"
test('has a selector with the text "-- select a variant --"', async ({ page }) => {
    await expect(page.locator('text=-- select a variant --')).toBeTruthy();
});

// and: the page should have a selector with the text "Freestyle"
test('has a selector with the text "Freestyle"', async ({ page }) => {
    await expect(page.locator('text=Freestyle')).toBeTruthy();
});

// and: the page should have a selector with the text "Standard"
test('has a selector with the text "OMOK"', async ({ page }) => {
    await expect(page.locator('text=OMOK')).toBeTruthy();
});

// // and: after clicking in the "Freestyle" option with no other user waiting for a match
// // Then: should be redirected to lobby page
// test('redirects to lobby page', async ({ page }) => {
//     await page.waitForTimeout(1000);

//     // click in the select
//     await page.locator('select').click();

//     // click in the "Freestyle" option
//     await page.selectOption("select", { label: "Freestyle" });

//     await page.waitForTimeout(1000);

//     // get the lobby id from the url
//     const lobbyId = page.url().split('/').pop();

//     await expect(page).toHaveURL(`${BASE}lobby/${lobbyId}`);
// });

// // and: after clicking in the "Freestyle" option with another user waiting for a match
// // in a different browser
// // Then: should be redirected to game page
// test('redirects to game page', async ({ page }) => {
//     // click in the selector
//     await page.locator('text=Freestyle').click();

//     await page.waitForTimeout(1000);

//     // get the lobby id from the url
//     const lobbyId = page.url().split('/').pop();

//     await expect(page).toHaveURL(`${BASE}lobby/${lobbyId}`);

//     // open a new browser
//     const browser2 = await chromium.launch();
//     const context2 = await browser2.newContext();
//     const page2 = await context2.newPage();

//     // register a new user
//     const username2 = generateRandomUsername();
//     const email2 = generateRandomEmail();
//     const password2 = generateRandomPassword();
//     await page2.goto(`${BASE}register`);
//     await page2.getByLabel('Username').fill(username2);
//     await page2.getByLabel('Email').fill(email2);
//     await page2.locator('input[name="password"]').fill(password2);
//     await page2.locator('input[name="confirmPassword"]').fill(password2);
//     await page2.getByRole('button', { name: 'Sign Up' }).click();

//     await page2.waitForTimeout(1000);

//     await page2.goto(`${BASE}login`);

//     await page2.getByLabel('Username').fill(username2);
//     await page2.getByLabel('Password').fill(password2);
//     await page2.getByRole('button', { name: 'Login' }).click();

//     await page2.waitForTimeout(1000);

//     await page2.goto(`${BASE}variants`);

//     // go to variants page in the second browser
//     await page2.goto(`${BASE}variants`);

//     // click in the selector
//     await page2.locator('text=-- select a variant --').click();

//     // click in the "Freestyle" option
//     await page2.locator('text=Freestyle').click();

//     await page2.waitForTimeout(1000);

//     // get the game id from the url
//     const gameId = page2.url().split('/').pop();

//     // then: should be redirected to game page in the first browser and the second browser
//     // because there is another user waiting for a match
//     await expect(page).toHaveURL(`${BASE}game/${gameId}`);
//     await expect(page2).toHaveURL(`${BASE}game/${gameId}`);
// });
