import { SirenModel } from "../../media/siren/SirenModel"

type HomeOutputModel = {
    id: number,
    username: string,
    email: string,
}

export type HomeOutput = SirenModel<HomeOutputModel>

