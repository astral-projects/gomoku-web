import {SirenModel} from "../../media/siren/SirenModel"

export type SystemOutputModel = {
    gameName: string,
    version: string,
    description: string,
    releaseDate: string,
    authors: Authors[]
}

type Authors = {
    firstName: string,
    lastName: string,
    gitHubUrl: string
}

export type SystemOutput = SirenModel<SystemOutputModel>

