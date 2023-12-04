import API from '../api/apiConnection.js';
import apiRoutes from '../api/routes.js';
import { FetchResponse } from './fetchResponse';
import { LoginOutput } from './users/models/LoginOuputModel.js';

const apiConnection = API();

export async function login(body: { username: string; password: string; }): Promise<FetchResponse<LoginOutput>> {
  const response = await apiConnection.postApi(apiRoutes.login, '', body);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}

export async function logout(token: string) {
  const response = await apiConnection.postApi(apiRoutes.logout, token);
  return await response.json();
}
