import { Link } from './Link';

export type Entity<T> = {
    properties: T;
    links: Link[];
    rel: string[];
};
