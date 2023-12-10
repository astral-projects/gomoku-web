import { SirenModel } from "../../../media/siren/SirenModel"

export type FindGameOutputModel = {
    message: string,
    id: number, 
}

export type FindGameOutput= SirenModel<FindGameOutputModel>