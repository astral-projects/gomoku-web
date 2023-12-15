/**
 * Returns the value of a cookie or undefined if the cookie does not exist
 * @param name - The name of the cookie
 * @returns
 */
export function getCookie(name: string) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift();
}
