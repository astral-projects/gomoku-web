import { SirenModel } from "../../media/siren/SirenModel"

export type LoginOutputModel = {
    token: string
}

export type LoginOutput = SirenModel<LoginOutputModel>