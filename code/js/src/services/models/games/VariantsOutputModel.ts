import {SirenModel} from "../../media/siren/SirenModel"

export type VariantsOutputModel = {
    id: number,
    name: string,
    openingRule: string,
    boardSize: string, 
}

export type VariantsOutput= SirenModel<VariantsOutputModel>