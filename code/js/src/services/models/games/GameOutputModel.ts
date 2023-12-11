import {SirenModel} from "../../media/siren/SirenModel"

export type GameOutputModel = {
    id: number;
    state: {
        name: string;
    };
    variant: {
        id: number;
        name: string;
        openingRule: string;
        boardSize: number;
    };
    board: {
        grid: string[];
        turn: {
            player: string;
            timeLeftInSec: {
                value: number;
            };
        };
    };
    createdAt: string;
    updatedAt: string;
    hostId: number;
    guestId: number;
};

export type GameOutput= SirenModel<GameOutputModel>