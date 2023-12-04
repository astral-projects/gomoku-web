import { Action } from "./Action";
import { Link } from "./Link";

export class Entity<T> {
    class: string[];
    properties: T;
    actions: Action[];
    links: Link[];
}