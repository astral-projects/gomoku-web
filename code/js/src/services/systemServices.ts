import {callApi, Method} from './apiService';
import {SystemOutput} from './models/system/SystemOutputModel';

export async function fetchSystemInfo() {
    return await callApi<unknown, SystemOutput>('api/system', Method.GET);
}