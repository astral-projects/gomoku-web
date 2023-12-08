import * as React from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { fetchRecipes, recipeUris } from './api/recipes';
import { fetchHome } from './api/authenticate';
import { Navigate } from 'react-router-dom';

fetchRecipes()
  .then(() => {
    console.log('Fetched recipes');
    console.log(recipeUris);
    return recipeUris;
  })
  .catch(() => {
    // retry
    fetchRecipes()
      .then(() => {
        console.log('Fetched recipes');
        console.log(recipeUris);
        return recipeUris;
      })
      .catch(error => {
        // redirect to error page
        console.log('Error fetching recipes', error);
        return <Navigate to="/error" />;
      });
  });

fetchHome()
  .then(() => {
    console.log('Fetched home');
    return;
  })
  .catch(error => {
    console.log('Error fetching home', error);
    console.log('Retrying');
    // retry once
    fetchHome()
      .then(() => {
        console.log('Fetched home');
        return;
      })
      .catch(error => {
        console.log('Error fetching home', error);
        // redirect to error page
        return <Navigate to="/error" />;
      });
  });
const rootElement = createRoot(document.getElementById('main-div'));
rootElement.render(<App />);
