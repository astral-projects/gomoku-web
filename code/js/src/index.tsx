import * as React from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { fetchRecipes, recipeUris } from './api/apiRecipes';
import { Error } from './pages/error/Error';

const rootElement = createRoot(document.getElementById('main-div'));

const retry = (fn: () => Promise<unknown>, retriesLeft = 3, interval = 1000) => {
    return new Promise((resolve, reject) => {
        fn()
            .then(resolve)
            .catch(error => {
                setTimeout(() => {
                    if (retriesLeft === 1) {
                        // reject('maximum retries exceeded');
                        reject(error);
                        return;
                    }
                    retry(fn, retriesLeft - 1, interval).then(resolve, reject);
                }, interval);
            });
    });
};

export const promise = fetchRecipes()
    .then(() => {
        console.log('Fetched recipes');
        console.log(recipeUris);
        return recipeUris;
    })
    .catch(() => {
        console.log('Error fetching recipes');
        console.log('Retrying');
        // retry 3 times
        retry(fetchRecipes, 3, 1000)
            .then(() => {
                console.log('Fetched recipes');
                console.log(recipeUris);
                return recipeUris;
            })
            .catch(error => {
                console.log('Error fetching recipes', error);
                // redirect to error page
                return rootElement.render(<Error />);
            });
    });

rootElement.render(<App />);
