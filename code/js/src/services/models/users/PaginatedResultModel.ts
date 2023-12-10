import { SirenModel } from '../../media/siren/SirenModel';

export type PaginatedResultModel<T> = {
  currentPage: number;
  itemsPerPage: number;
  totalPages: number;
  items: T[];
};

export type PaginatedResult<T> = SirenModel<PaginatedResultModel<T>>;
