/**
 * Verifies if the response is successful
 * @param contentType
 * @returns
 */
export function isSuccessful(contentType: string) {
    return contentType === 'application/vnd.siren+json';
}
