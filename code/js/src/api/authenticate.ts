
//const API_ME_URI = findUri('me');
const API_ME_URI = 'http://localhost:3000/api/users/home';

export let loggedIn = false;

export async function fetchHome() {
  if (!isAuthenticationRequired()) {
    return;
  }
  return fetch(API_ME_URI, {
    method: 'GET',
  })
    .then(response => {
      if (response.status !== 401) {
        loggedIn = true;
        return response.json();
      } else {
        loggedIn = false;
        throw new Error('Unauthorized');
      }
    })
}

/**
 * Verifies if the page requires authentication
 * @returns true if the user is not on the login, register, or home page
 */
function isAuthenticationRequired(): boolean {
  return (
    window.location.pathname !== "/login" &&
    window.location.pathname !== "/register" &&
    window.location.pathname !== "/home"
  );
}
