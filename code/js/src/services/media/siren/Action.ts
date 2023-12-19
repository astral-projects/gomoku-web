export type Action = {
    name: string;
    method: string;
    href: string;
    title: string;
    type: string;
    fields: Field[];
}

export type Field = {
    name: string;
    type: string;
    value: string;
}