import {callApi, Method} from '../api/apiService';
import {FindGameOutput} from './models/games/FindGameOutputModel.js';
import {VariantsOutput} from './models/games/VariantsOutputModel.js';
import {LobbyOutput} from './models/lobby/LobbyOutputModel.js';
import {GameOutput} from './models/games/GameOutputModel.js';
import {VariantsInputModel} from './models/games/VariantsInputModel.js';
import {MoveInputModel} from './models/games/MoveInputModel.js';


export async function findGame(body: VariantsInputModel) {
  return await callApi<VariantsInputModel, FindGameOutput>('/api/games', Method.POST, body);
}


export async function waittingInLobby(lobbyId: number) {
  return await callApi<unknown, LobbyOutput>(`/api/lobby/${lobbyId}`, Method.GET);
}

export async function getVariants() {
  return await callApi<unknown, VariantsOutput>('/api/games/variants', Method.GET);
}


export async function exitLobby(lobbyId: number) {
  return await callApi<unknown, LobbyOutput>(`/api/lobby/${lobbyId}/exit`, Method.DELETE);
}


export async function getGame(gameId: number) {
  return await callApi<unknown, GameOutput>(`/api/games/${gameId}`, Method.GET);
}


export async function makeMove(gameId: number, body: MoveInputModel) {
  return await callApi<MoveInputModel, GameOutput>(`/api/games/${gameId}/move`, Method.POST, body);
}

export async function exitGame(gameId: number) {
  return await callApi<unknown, GameOutput>(`/api/games/${gameId}/exit`, Method.POST);
}