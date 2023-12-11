import { callApi } from './apiService';
import { Method } from './apiService';
import { FindGameOutput } from './users/models/games/FindGameOutputModel.js';
import { VariantsOutput } from './users/models/games/VariantsOutputModel.js';
import { LobbyOutput } from './users/models/lobby/LobbyOutputModel.js';
import { GameOutput } from './users/models/games/GameOutputModel.js';
import { VariantsInputModel } from './users/models/games/VariantsInputModel.js';
import { MoveInputModel } from './users/models/games/MoveInputModel.js';



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