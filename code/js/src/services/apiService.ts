// apiService.js

import API from '../api/apiConnection';
import { ApiResponse } from '../api/apiConnection';
//import { findUri } from '../api/recipes';
import { ProblemModel } from './media/ProblemModel';

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
    console.log("Calling api with uri: " + uri);
    switch (method) {
      case Method.GET:
        console.log("Calling api with uri: insiede the method " + uri);
        response = await apiConnection.getApi(uri);
        console.log("Calling api with uri: the response  " + response);
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
    const errorResponse = (await error) as ApiResponse<ProblemModel>;
    return errorResponse;
  }
}
