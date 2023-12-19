import API, { ApiResponse } from './apiConnection';
import { ProblemModel } from '../services/media/ProblemModel';

const apiConnection = API();

export enum Method {
    GET = 'GET',
    POST = 'POST',
    PUT = 'PUT',
    DELETE = 'DELETE',
}

/**
 * Function that calls the API and returns the response as a json or a problem model
 * @param uri - the uri to call
 * @param method - the method to use for the request
 * @param body - optional body for POST and PUT requests
 * @param token - optional token for authentication
 */
export async function callApi<B, T>(uri: string, method: Method, body?: B): Promise<ApiResponse<T | ProblemModel>> {
    let response: ApiResponse<T>;
    try {
        // get the uri from the rel
        // const uriRecipe = findUri(rel);
        const bodyFormat = body ? body : {};
        switch (method) {
            case Method.GET:
                response = await apiConnection.getApi(uri);
                return response;

            case Method.POST:
                response = await apiConnection.postApi(uri, bodyFormat);
                return response;

            case Method.PUT:
                response = await apiConnection.putApi(uri, bodyFormat);
                return response;

            case Method.DELETE:
                response = await apiConnection.deleteApi(uri);
                return response;
        }
    } catch (error) {
        return (await error) as ApiResponse<ProblemModel>;
    }
}
