import { SirenModel } from "../../../media/siren/SirenModel"

type LoginOutputModel = {
    token: string
}

export type LoginOutput = SirenModel<LoginOutputModel>