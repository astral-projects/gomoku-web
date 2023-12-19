/**
 * Function that builds a web route exchanging the variable param (:value)
 * for the value passed as a parameter
 *
 * @param uri route to replace
 * @param values array of values to replace
 */
export function replacePathVariables(uri: string, values: number[]): string {
    const parts = uri.split('/');
    let j = 0;
    for (let i = 0; i < parts.length; i++) {
        if (parts[i].startsWith(':')) {
            parts[i] = values[j++].toString();
        }
    }
    return parts.join('/');
}
