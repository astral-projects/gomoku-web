import { test, expect } from '@playwright/test';

export const port = 8088;
export const BASE = `http://localhost:${port}/`;

test('has title', async ({ page }) => {
    await page.goto(`${BASE}login`);

    // Expect a title "to contain" a substring.
    await expect(page).toHaveTitle('Gomoku Royale');
});

test('clicking in sign up of login page goes to register page', async ({ page }) => {
    await page.goto(`${BASE}login`);

    await page.click('text=Sign Up');

    await expect(page).toHaveURL(`${BASE}register`);
});

// test('creating a new account', async ({ page }) => {
//     // Expect the page to be loaded.
//     await page.goto(`${BASE}register`);

//     // Expect that register has input fields for username, email, password and confirm password.
//     const usernameTextInput = await page.locator('input[name="username"]');
//     const emailTextInput = await page.locator('input[name="email"]');
//     const passwordTextInput = await page.locator('input[name="password"]');
//     const confirmPasswordTextInput = await page.locator('input[name="confirmPassword"]');

//     const usernameLabel = await page.locator('label[for="username"]');
//     const emailLabel = await page.locator('label[for="email"]');
//     const passwordLabel = await page.locator('label[for="password"]');
//     const confirmPasswordLabel = await page.locator('label[for="confirmPassword"]');

//     const registerButton = await page.locator('button[type="submit"]');
    
//     await expect(registerButton).toBeTruthy();
//     await expect(usernameLabel).toBeTruthy();
//     await expect(emailLabel).toBeTruthy();
//     await expect(passwordLabel).toBeTruthy();
//     await expect(confirmPasswordLabel).toBeTruthy();
//     await expect(usernameTextInput).toBeTruthy();
//     await expect(emailTextInput).toBeTruthy();
//     await expect(passwordTextInput).toBeTruthy();
//     await expect(confirmPasswordTextInput).toBeTruthy();

//     // Fill the input with correct values.
//     await usernameTextInput.fill('test-username4');
//     await emailTextInput.fill('testusername@gmail.com');
//     await passwordTextInput.fill('testpassword123');
//     await confirmPasswordTextInput.fill('testpassword123');

//     // Click the register button.
//     await registerButton.click();

//     // Expect that the page is redirected to login page.
//     await expect(page).toHaveURL(`${BASE}login`);

// });

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

    // Expect that the gives an error message.
    await expect(page.locator('text=Username must be between 5 and 30 characters')).toBeTruthy();

    // Fill the input fields wiht a wrong password
    await usernameTextInput.fill('test-username');
    await passwordTextInput.fill('test');
    await loginButton.click();

    // Expect that the gives an error message.
    await expect(page.locator('text=Password must be between 8 and 30 characters')).toBeTruthy();

    // Fill the input fields with correct values.
    await usernameTextInput.fill('test-username');
    await passwordTextInput.fill('testpassword123');
    await loginButton.click();

    // Expect that the page is redirected to home page.
    await expect(page).toHaveURL(`${BASE}me`);

});
