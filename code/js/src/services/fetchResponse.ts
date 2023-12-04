import { ProblemModel } from './media/ProblemModel';
import { SirenModel } from './media/siren/SirenModel';

export type FetchResponse<T> = {
  contentType: string;
  json: SirenModel<T> | ProblemModel;
};
