import {callApi, Method} from '../api/apiService';
import {LoginInputModel} from './models/users/LoginInputModel';
import {RegisterInputModel} from './models/users/RegisterInputModel';
import {LoginOutput} from './models/users/LoginOuputModel';
import {HomeOutput} from './models/users/HomeOutputModel';
import {RegisterOutput} from './models/users/RegisterOuputModel';
import {PaginatedResult} from './models/users/PaginatedResultModel';
import {UserStats} from '../domain/UserStats';
import {UserStatsOutput} from './models/users/UserStatsOutputModel';
import {findUri, replaceParams} from '../api/apiRecipes';

const itemsPerPage = 10;

export async function register(body: RegisterInputModel) {
    const uriRecipe = await findUri('register')
    return await callApi<RegisterInputModel, RegisterOutput>(uriRecipe, Method.POST, body);
}

export async function login(body: LoginInputModel) {
    const uriRecipe = await findUri('login');
    return await callApi<LoginInputModel, LoginOutput>(uriRecipe, Method.POST, body);
}

export async function logout() {
    const uriRecipe = await findUri('logout');
    return await callApi(uriRecipe, Method.POST);
}

export async function me() {
    const uriRecipe = await findUri('me');
    return await callApi<unknown, HomeOutput>(uriRecipe, Method.GET);
}

export async function fetchUserStatsByUserId(userId: string) {
    const uriRecipe = await findUri('user/stats');
    const url = replaceParams(uriRecipe, {user_id: userId});
    return await callApi<unknown, UserStatsOutput>(url, Method.GET, {});
}

export async function fetchUserStatsBySearchTerm(term: string) {
    const uriRecipe = await findUri('users/search');
    const uri = replaceParams(uriRecipe, {term: term});
    return await callApi<unknown, PaginatedResult<UserStats>>(uri, Method.GET, {});
}

export async function fetchUsersStats(uri?: string) {
    const uriRecipe = await findUri('users/stats')
    const uriTemplateExpanded = replaceParams(uriRecipe, {page: 1, itemsPerPage: itemsPerPage});
    return await callApi<unknown, PaginatedResult<UserStats>>(uri || uriTemplateExpanded, Method.GET, {});
}
