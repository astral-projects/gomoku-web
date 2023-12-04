const BASE_URL = `http://localhost:3000/api`;
const HOME_API_URL = `${BASE_URL}/users/home`;
const LOGIN_API_URL = `${BASE_URL}/users/token`;

export const apiRoutes = {
   home: HOME_API_URL,
   login: LOGIN_API_URL,
}

export default apiRoutes;
