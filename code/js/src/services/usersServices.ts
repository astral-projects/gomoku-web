import { callApi } from '../api/apiService';
import { Method } from '../api/apiService';
import { LoginInputModel } from './models/users/LoginInputModel';
import { RegisterInputModel } from './models/users/RegisterInputModel';
import { LoginOutput } from './models/users/LoginOuputModel';
import { HomeOutput } from './models/users/HomeOutputModel';
import { RegisterOutput } from './models/users/RegisterOuputModel';
import { PaginatedResult } from './models/users/PaginatedResultModel';
import { UserStats } from '../domain/UserStats';
import { UserStatsOutput } from './models/users/UserStatsOutputModel';
import { findUri } from '../api/apiRecipes';

export async function register(body: RegisterInputModel) {
    return await callApi<RegisterInputModel, RegisterOutput>('/api/users', Method.POST, body);
}

export async function login(body: LoginInputModel) {
    return await callApi<LoginInputModel, LoginOutput>('/api/users/token', Method.POST, body);
}

export async function logout() {
    return await callApi('api/users/logout', Method.POST);
}

export async function me() {
    return await callApi<unknown, HomeOutput>(await findUri('me'), Method.GET);
}

export async function fetchUserStatsByUserId(userId: string) {
    const uri = `/api/users/${userId}/stats`;
    return await callApi<unknown, UserStatsOutput>(uri, Method.GET, {});
}

export async function fetchUserStatsBySearchTerm(term: string) {
    const uri = `/api/users/stats/search?term=${term}`;
    return await callApi<unknown, PaginatedResult<UserStats>>(uri, Method.GET, {});
}

export async function fetchUsersStats(uri?: string) {
    const page = 1;
    const itemsPerPage = 10;
    const query = `page=${page}&itemsPerPage=${itemsPerPage}`;
    const base = `api/users/stats`;
    const defaultUri = `${base}?${query}`;
    const actualUri = uri ? uri : defaultUri;
    return await callApi<unknown, PaginatedResult<UserStats>>(actualUri, Method.GET, {});
}
