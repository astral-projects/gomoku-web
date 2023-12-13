import { promise } from "../index";

export const recipeUris = [];
const PORT = 4000;
const BASE = `http://localhost:${PORT}`;

export type Recipe = {
  rel: string[];
  href: string;
};

export async function fetchRecipes(): Promise<Recipe[]> {
  try {
    const response = await fetch('http://localhost:4000/api/', {
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
      console.log('Error during fetch: ' + response.status);
      throw new Error('Error during fetch: ' + response.status);
    }
  } catch (error) {
    console.log('Error during fetch: ' + error);
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
    console.log('No recipies found');
  }
}
