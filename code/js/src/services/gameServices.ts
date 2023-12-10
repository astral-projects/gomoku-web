import { callApi } from './apiService';
import { Method } from './apiService';
import { FindGameOutput } from './users/models/games/FindGameOutputModel.js';
import { VariantsOutput } from './users/models/games/VariantsOutputModel.js';
import { LobbyOutput } from './users/models/lobby/LobbyOutputModel.js';
import { GameOutput } from './users/models/games/GameOutputModel.js';
import { VariantsInputModel } from './users/models/games/VariantsInputModel.js';
import { MoveInputModel } from './users/models/games/MoveInputModel.js';



export async function findGame(body: VariantsInputModel) {
  return await callApi<VariantsInputModel, FindGameOutput>('api/games', Method.POST, body);
}

/*
export async function register(body: RegisterInputModel) {
    return await callApi<RegisterInputModel, RegisterOutput>('register', Method.POST, body);
}

export async function findGame(body: { variantId:number }): Promise<FetchResponse<FindGameOutput>> {
    const response = await apiConnection.postApi(apiRoutes.findGame, token, body);
    return {
      contentType: response.headers.get('Content-Type'),
      json: await response.json(),
    };
}*/

export async function waittingInLobby(lobbyId:number){
  return await callApi<unknown, LobbyOutput>(`api/lobby/${lobbyId}`, Method.GET);
}

/*export async function waittingInLobby(lobbyId:number): Promise<FetchResponse<FindGameOutput>>{
  const response = await apiConnection.getApi(`http://localhost:3000/api/lobby/${lobbyId}`, token);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}*/

/*export async function getVariants(): Promise<FetchResponse<VariantsOutput>>{
  const response = await apiConnection.getApi(`http://localhost:3000/api/games/variants`, token);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}*/
export async function getVariants() {
  return await callApi<unknown, VariantsOutput>('api/games/variants', Method.GET);
}

/*export async function exitLobby(lobbyId:number): Promise<FetchResponse<LobbyOutput>>{
  const response = await apiConnection.deleteApi(`http://localhost:3000/api/lobby/${lobbyId}/exit`, token);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}*/

export async function exitLobby(lobbyId:number){
  return await callApi<unknown, LobbyOutput>(`api/lobby/${lobbyId}/exit`, Method.DELETE);
}

/*export async function getGame(gameId:number): Promise<FetchResponse<GameOutput>>{
  const response = await apiConnection.getApi(`http://localhost:3000/api/games/${gameId}`, token);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}*/

export async function getGame(gameId:number){
  console.log("gameId: dento do call api  ", gameId);
  return await callApi<unknown, GameOutput>(`http://localhost:3000/api/games/${gameId}`, Method.GET);
}

/*export async function makeMove(gameId:number, body:{col:string, row:number}): Promise<FetchResponse<GameOutput>>{
  const response = await apiConnection.postApi(`http://localhost:3000/api/games/${gameId}/move`, token, body);
  return {
    contentType: response.headers.get('Content-Type'),
    json: await response.json(),
  };
}*/

export async function makeMove(gameId:number, body:MoveInputModel) {
  return await callApi<MoveInputModel, GameOutput>(`http://localhost:3000/api/games/${gameId}/move`, Method.POST, body);
}

export async function exitGame(gameId:number){
  return await callApi<unknown, GameOutput>(`http://localhost:3000/api/games/${gameId}/exit`, Method.POST);
}