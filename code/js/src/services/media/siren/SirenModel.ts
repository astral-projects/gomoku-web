import { Action } from './Action';
import { Entity } from './Entity';
import { Link } from './Link';

export class SirenModel<T> {
  class: string[];
  properties: T;
  entities: Entity<unknown>[];
  actions: Action[];
  links: Link[];
}
