/**
 * Generate a random username starting with 'test-username' and followed by a random string.
 * @returns
 */
export function generateRandomUsername() {
    return `test-username-${Math.random().toString(36).substring(7)}`;
}

/**
 * Generate a random email starting with 'test-email' and followed by a random string and ending with '@gmail.com'.
 * @returns
 */
export function generateRandomEmail() {
    return `test-email-${Math.random().toString(36).substring(7)}@gmail.com`;
}

/**
 * Generate a random password starting with 'test-password' and followed by a random string.
 * @returns
 */
export function generateRandomPassword() {
    return `test-password-${Math.random().toString(36).substring(7)}`;
}
