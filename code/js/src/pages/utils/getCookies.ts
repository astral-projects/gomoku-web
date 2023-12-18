/**
 * Returns the value of a cookie or undefined if the cookie does not exist
 * @param name - The name of the cookie
 * @returns
 */
export function getCookie(name: string): string | undefined {
  const cookie = document.cookie
    .split('; ')
    .find((row) => row.startsWith(name + '='));

  return cookie?.split('=')[1];
}
