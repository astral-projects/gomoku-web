//const API_ME_URI = findUri('me');
const API_ME_URI = 'http://localhost:4000/api/users/home';

export async function fetchHome() {
    if (!isAuthenticationRequired()) {
        return;
    }
    return fetch(API_ME_URI, {
        method: 'GET',
    })
        .then(response => {
            if (response.status !== 401) {
                return response.json();
            } else {
                return null;
            }
        })
        .catch(error => {
            console.log('Error during fetch: ' + error);
            throw error;
        });
}

/**
 * Verifies if the page requires authentication
 * @returns true if the user is not on the login, register, or home page
 */
function isAuthenticationRequired(): boolean {
    return (
        window.location.pathname !== '/login' &&
        window.location.pathname !== '/register' &&
        window.location.pathname !== '/home'
    );
}
