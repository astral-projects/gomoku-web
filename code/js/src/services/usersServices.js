import API from '../api/apiConnection.js';
import {LOGIN_API_URL} from '../api/routes.js';

const apiConnection = API()


export function login(username, password) {
    return apiConnection.postApi(LOGIN_API_URL, "", {username: username, password: password})
}