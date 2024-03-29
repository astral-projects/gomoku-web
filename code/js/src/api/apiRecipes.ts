import { promise } from '../index';

export const recipeUris = [];

// const PORT = process.env.PORT || 4000

const PORT = 4000;
const BASE = `http://localhost:${PORT}`;

export type Recipe = {
    rel: string[];
    href: string;
};

export async function fetchRecipes(): Promise<Recipe[]> {
    try {
        const response = await fetch(BASE + '/api/', {
            method: 'GET',
        });

        if (response.ok) {
            const data = await response.json();
            if (!data) {
                return [];
            }

            const recipes = data.recipeLinks;
            const recipeUris = getRecipeUris(recipes);
            recipeUris.push({ rel: 'home', href: data.links[0].href });
            return recipeUris;
        } else {
            throw new Error('Error during fetch: ' + response.status);
        }
    } catch (error) {
        console.log(error);
        throw error;
    }
}

/**
 * Returns the receipts for all routes of the api in the following format:
 * receipts = [
 *   {
 *    rel: 'self',
 *    href: 'http://localhost:3000/api/users/1',
 *   },
 *   {
 *    rel: 'home',
 *    href: 'http://localhost:3000/api/users/home',
 *   }
 *  ];
 * @param {*} recipes - The receipts to parse
 */
function getRecipeUris(recipes: Recipe[]) {
    // receipts = [{rel: ['self'], href: 'http://localhost:3000/api/users/1'}, {rel: ['home'], href: 'http://localhost:3000/api/users/home'}]
    recipes.forEach(recipe => {
        if (!recipe.rel || !recipe.href) {
            return;
        }
        const rel = recipe.rel[0];
        const href = recipe.href;
        recipeUris.push({ rel, href });
    });
    return recipeUris;
}

/**
 * Search for the href with the given rel name
 * Can be the exact uri or a receipt uri
 * @param relName - The name of the rel to find
 * @returns
 */
export async function findUri(relName: string) {
    await promise;
    if (recipeUris instanceof Array && recipeUris.length > 0) {
        for (let i: number = 0; i < recipeUris.length; i++) {
            const recipeUri = recipeUris[i];
            if (recipeUri.rel.includes(relName)) {
                return `${BASE}${recipeUri.href}`;
            }
        }
    } else {
        //TODO: throw error
        console.log('No recipies found');
    }
}

/**
 * Replaces the params in the uri with the given params
 * Example:
 * /api/users/{userId} with {userId: 1} => /api/users/1
 * /api/users/{userId}/friends/{friendId} with {userId: 1, friendId: 2} => /api/users/1/friends/2
 * /api/users/stats?q={query}{&page,itemPerPage} with {query: 'test', page: 1, itemPerPage: 10} => /api/users/stats?q=test&page=1&itemPerPage=10
 *
 * @param uri - The uri to replace the params
 * @param params - The params to replace
 * @returns
 */
export function replaceParams(uri: string, params: { [key: string]: number | string }) {
    const paramNames = getParamNames(uri);
    paramNames.forEach(paramName => {
        const paramValue = params[paramName];
        // {&page,itemPerPage} => &page=3&itemPerPage=10
        // or {&page} => &page=3
        if (paramName.startsWith('&')) {
            // parse the query params
            // paramName can be [&page,itemsPerPage] or [&page]
            const queryParamNames = paramName.substring(1).split(',');
            let query = '';
            queryParamNames.forEach(queryParamName => {
                const queryParamValue = params[queryParamName];
                if (queryParamValue) {
                    query += `&${queryParamName}=${queryParamValue}`;
                }
            });
            uri = uri.replace(`{${paramName}}`, query.substring(1));
        } else if (paramValue) {
            uri = uri.replace(`{${paramName}}`, paramValue.toString());
        }
    });
    // ensure after ? there is no &
    uri = uri.replace('?&', '?');
    return uri;
}

/**
 * Returns the param names of the uri
 * @param uri - The uri to get the param names
 * @returns
 */
function getParamNames(uri: string) {
    const paramNames = [];
    const regex = /{([^}]+)}/g;
    let match: Array<string> | null;
    while ((match = regex.exec(uri))) {
        paramNames.push(match[1]);
    }
    return paramNames;
}
