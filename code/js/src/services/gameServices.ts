import {callApi, Method} from '../api/apiService';
import {FindGameOutput} from './models/games/FindGameOutputModel.js';
import {VariantsOutput} from './models/games/VariantsOutputModel.js';
import {LobbyOutput} from './models/lobby/LobbyOutputModel.js';
import {GameOutput} from './models/games/GameOutputModel.js';
import {VariantsInputModel} from './models/games/VariantsInputModel.js';
import {MoveInputModel} from './models/games/MoveInputModel.js';
import { findUri, replaceParams } from '../api/apiRecipes';


export async function findGame(body: VariantsInputModel) {
  return await callApi<VariantsInputModel, FindGameOutput>(await findUri('find-game'), Method.POST, body);
}


export async function waittingInLobby(lobbyId: number) {
  const url = replaceParams(await findUri('lobby'), {lobby_id: lobbyId});
  return await callApi<unknown, LobbyOutput>(url, Method.GET);
}

export async function getVariants() {
  return await callApi<unknown, VariantsOutput>(await findUri('variants'), Method.GET);
}


export async function exitLobby(lobbyId: number) {
  const url = replaceParams(await findUri('exit-lobby'), {lobby_id: lobbyId});
  return await callApi<unknown, LobbyOutput>(url, Method.DELETE);
}


export async function getGame(gameId: number) {
  const url = replaceParams(await findUri('game'), {game_id: gameId});
  return await callApi<unknown, GameOutput>(url, Method.GET);
}


export async function makeMove(gameId: number, body: MoveInputModel) {
  const url = replaceParams(await findUri('move'), {game_id: gameId});
  return await callApi<MoveInputModel, GameOutput>(url, Method.POST, body);
}

export async function exitGame(gameId: number) {
  const url = replaceParams(await findUri('exit-game'), {game_id: gameId});
  return await callApi<unknown, GameOutput>(url, Method.POST);
}