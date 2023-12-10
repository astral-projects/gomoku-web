export class Link {
    rel: string[];
    href: string;
}

export function getHrefByRel(links: Link[], relName: string): string | null {
    for (const link of links) {
        for (const rel of link.rel) {
            if (rel === relName) {
                return link.href;
            }
        }
    }
    return null;
}