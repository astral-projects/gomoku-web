import { findUri } from './recipes';
// import { useSetUser } from '../pages/gomokuContainer/GomokuContainer';
// import { Entity } from '../services/media/siren/Entity';
// import { Email, Id, User, Username } from '../domain/User';

const API_ME_URI = findUri('me');

export async function fetchHome() {
  // const setUser = useSetUser();
  if (!isAuthenticationRequired()) {
    return;
  }
  try {
    const response = await fetch(API_ME_URI, {
      method: 'GET',
    });
    if (response.status === 401) {
      // not authenticated
      console.log('Unauthenticated');
    } else {
      console.log('Authenticated');
      const res = await response.json();
      console.log(res);
      return res;
      // do the set user, set the user in the context. How if we are not in the component?
    }
  } catch (error) {
    console.log('Error', error);
    throw error;
  }
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
