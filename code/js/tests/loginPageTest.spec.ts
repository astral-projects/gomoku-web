import { test, expect } from '@playwright/test';
import { generateRandomUsername, generateRandomEmail, generateRandomPassword } from './utils/generateRandomUser';

export const port = 4000;
export const BASE = `http://localhost:${port}/`;

const username = generateRandomUsername();
const email = generateRandomEmail();
const password = generateRandomPassword();

test.beforeEach(async ({ page }) => {
    // Expect the page to be loaded.
    await page.goto(`${BASE}register`);

    // Expect that register has input fields for username, email, password and confirm password.
    const usernameTextInput = await page.locator('input[name="username"]');
    const emailTextInput = await page.locator('input[name="email"]');
    const passwordTextInput = await page.locator('input[name="password"]');
    const confirmPasswordTextInput = await page.locator('input[name="confirmPassword"]');

    const registerButton = await page.getByRole('button', { name: 'Sign Up' });

    // Fill the input with correct values.
    await usernameTextInput.fill(username);
    await emailTextInput.fill(email);
    await passwordTextInput.fill(password);
    await confirmPasswordTextInput.fill(password);

    // Click the register button.
    await registerButton.click();

    await page.waitForTimeout(1000);

    // Expect that the page is redirected to login page.
    await expect(page).toHaveURL(`${BASE}login`);
});

test('clicking in sign up of login page goes to register page', async ({ page }) => {
    await page.goto(`${BASE}login`);

    await page.click('text=Sign Up');

    await page.waitForTimeout(1000);

    await expect(page).toHaveURL(`${BASE}register`);
});

// test the login page
test('login page', async ({ page }) => {
    await page.goto(`${BASE}login`);

    // Expect the page to be loaded.
    await expect(page).toHaveURL(`${BASE}login`);

    // Expect that login page contains two input fields for username and password.
    const usernameTextInput = await page.locator('input[name="username"]');
    const passwordTextInput = await page.locator('input[name="password"]');

    const usernameLabel = await page.locator('label[for="username"]');
    const passwordLabel = await page.locator('label[for="password"]');

    const loginButton = await page.locator('button[type="submit"]');

    await expect(loginButton).toBeTruthy();
    await expect(usernameLabel).toBeTruthy();
    await expect(passwordLabel).toBeTruthy();
    await expect(usernameTextInput).toBeTruthy();
    await expect(passwordTextInput).toBeTruthy();

    // Fill the input with a wrong username.
    await usernameTextInput.fill('test');
    await passwordTextInput.fill('testdasdada');
    await loginButton.click();

    await page.waitForTimeout(1000);

    // Expect that the gives an error message.
    await expect(page.locator('text=Username must be between 5 and 30 characters')).toBeTruthy();

    // Fill the input fields wiht a wrong password
    await usernameTextInput.fill('test-username');
    await passwordTextInput.fill('test');
    await loginButton.click();

    // Expect that the gives an error message.
    await expect(page.locator('text=Password must be between 8 and 30 characters')).toBeTruthy();

    console.log(username);
    console.log(password);
    // Fill the input fields with correct values.
    await usernameTextInput.fill(username);
    await passwordTextInput.fill(password);

    await loginButton.click();

    await page.waitForTimeout(1000);

    // Expect that the page is redirected to me page.
    await expect(page).toHaveURL(`${BASE}me`);
});
