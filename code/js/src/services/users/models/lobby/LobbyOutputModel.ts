import { SirenModel } from "../../../media/siren/SirenModel"

export type LobbyOutputModel = {
    lobbyid: number,
    message: string,  
}

export type LobbyOutput= SirenModel<LobbyOutputModel>