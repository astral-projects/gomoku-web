import API from '../api/apiConnection';
import { LoginInputModel } from './users/models/LoginInputModel';
import { ApiResponse } from '../api/apiConnection';
import { LoginOutput } from './users/models/LoginOuputModel';
import { HomeOutput } from './users/models/HomeOutputModel';
import { ProblemModel } from './media/ProblemModel';
import { findUri } from '../api/recipes';

const apiConnection = API();

export async function login(body: LoginInputModel) {
  try {
    const response =  await apiConnection.postApi(findUri('login'), body) as ApiResponse<LoginOutput>;
    return response;
  } catch (error) {
    const response = await error as ApiResponse<ProblemModel>;
    return response;
  }
  
}

export async function me() {
  try {
    const uri = findUri("me");
    const response = await apiConnection.getApi(uri) as ApiResponse<HomeOutput>;
    return response;
  } catch (error) {
    const response = error as ApiResponse<ProblemModel>;
    return response;
  }
}
