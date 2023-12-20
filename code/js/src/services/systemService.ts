import {findUri} from '../api/apiRecipes';
import {callApi, Method} from '../api/apiService';
import {SystemOutput} from './models/system/SystemOutputModel';

export async function fetchSystemInfo() {
    return await callApi<unknown, SystemOutput>(await findUri('system'), Method.GET);
}
