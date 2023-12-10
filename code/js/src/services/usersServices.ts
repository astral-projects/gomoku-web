import { callApi } from './apiService';
import { Method } from './apiService';
import { LoginInputModel } from './models/users/LoginInputModel';
import { RegisterInputModel } from './models/users/RegisterInputModel';
import { LoginOutput } from './models/users/LoginOuputModel';
import { HomeOutput } from './models/users/HomeOutputModel';
import { RegisterOutput } from './models/users/RegisterOuputModel';
import { PaginatedResult } from './models/users/PaginatedResultModel';
import { UserStats } from '../domain/UserStats';

export async function register(body: RegisterInputModel) {
    return await callApi<RegisterInputModel, RegisterOutput>('register', Method.POST, body);
}

export async function login(body: LoginInputModel) {
    return await callApi<LoginInputModel, LoginOutput>('api/users/token', Method.POST, body);
}

export async function logout() {
    return await callApi('logout', Method.POST);
}

export async function me() {
    return await callApi<unknown, HomeOutput>('me', Method.GET);
}

export async function fetchUserStatsBySearchTerm(term: string) {
    const uri = `/api/users/stats/search?term=${term}`;
    const token = "HbwDxIBtw4D7t6VNkiA8cOuGqzNF1NZkITT6wOUCuDc="
    return await callApi<unknown, PaginatedResult<UserStats>>(uri, Method.GET, {}, token);
}

export async function fetchUserStats(uri?: string) {
    const page = 1;
    const itemsPerPage = 10;
    const query = `page=${page}&itemsPerPage=${itemsPerPage}`;
    const base = `api/users/stats`;
    const defaultUri = `${base}?${query}`;
    const actualUri = uri ? uri : defaultUri;
    return await callApi<unknown, PaginatedResult<UserStats>>(actualUri, Method.GET, {});
}
