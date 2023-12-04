export class Action {
    name: string;
    method: string;
    href: string;
    title: string;
    type: string;
    fields: Field[];
}

export class Field {
    name: string;
    type: string;
    value: string;
}