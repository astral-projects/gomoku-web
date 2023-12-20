# Gomoku - Backend Documentation 🉐

> This is the frontend documentation for the Gomoku Royale game.

## Table of Contents

- [Gomoku - Backend Documentation 🉐](#gomoku---backend-documentation-)
    - [Table of Contents](#table-of-contents)
    - [Introduction](#introduction)
    - [Pages](#pages)
    - [Code Structure](#code-structure)
    - [API](#api)
        - [Connection](#connection)
        - [Service](#service)
        - [Recipes](#recipes)
    - [React Context](#react-context)
    - [Webpack Configuration](#webpack-configuration)
    - [Authentication](#authentication)
    - [Tests](#tests)
        - [Implementation Challenges](#implementation-challenges)
        - [Further Improvements](#further-improvements)

---

## Introduction

The frontend is an [SPA](https://en.wikipedia.org/wiki/Single-page_application) and acts as a web-based client for
the [Gomoku Royale API](../../docs/gomoku-backend-api.md).
It is written mainly in [TypeScript](https://www.typescriptlang.org/) and uses the [React](https://reactjs.org/)

Some dependencies used in this project are:

- [React Router](https://reactrouter.com/) - Used to manage the application routes;
- [React Dom](https://reactjs.org/docs/react-dom.html) - Used to render the React components with the DOM;
- [Webpack](https://webpack.js.org/) - Used to bundle the application;

---

## Pages

The application uses the [React Router](https://reactrouter.com/) to manage the routes, and can be seen in the
[App](./src/App.tsx) component.

The routes are the following:

[Home Page](./src/pages/home/Home.tsx)

- Path: `/`
- Component: `Home`
- Description: The main landing page of the application.

[Login Page](./src/pages/login/Login.tsx)

- Path: `/login`
- Component: `Login`
- Description: Page for user authentication and login.

[Me Page](./src/pages/me/Me.tsx)

- Path: `/me`
- Component: `Me`
- Description: User profile page displaying personalized information.

[Register Page](./src/pages/register/Register.tsx)

- Path: `/register`
- Component: `Register`
- Description: Page for user registration.

[Rankings Page](./src/pages/rankings/Rankings.tsx)

- Path: `/rankings`
- Component: `Rankings`
- Description: Page displaying overall rankings of users.

[User Stats Page](./src/pages/userstats/UserStats.tsx)

- Path: `/rankings/:id`
- Component: `UserStats`
- Description: Page displaying detailed statistics for a specific user identified by id

[Lobby Page](./src/pages/lobby/Lobby.tsx)
- Path: `/lobby/:lobbyId`
- Component: `Lobby`
- Description: Page displaying details and status of a specific lobby identified by lobbyId.

[Find Game Page](./src/pages/findGame/FindGame.tsx)

- Path: `/games`
- Component: `FindGame`
- Description: Page for users to find and join available games.

[In-Game Page](./src/pages/game/Game.tsx)

- Path: `/games/:gameId`
- Component: `Game`
- Description: Page displaying details and status of a specific game identified by gameId.

[Logout Page](./src/pages/logout/Logout.tsx)

- Path: `/logout`
- Component: `Logout`
- Description: Page for user logout and session termination.

[About Page](./src/pages/about/About.tsx)

- Path: `/about`
- Component: `About`
- Description: Page providing information about the application.

[Error Page](./src/pages/error/Error.tsx)

- Path: `/error`
- Component: `Error`
- Description: Page displaying an error or handling unexpected situations.

## Code Structure

The frontend code is organized in the following way:

- `js`
    - `public` - Contains the `index.html` and the `index.css` files;
    - `src`
        - `api` - Exposes generic modules to communicate with the API;
        - `components` - Contains some React components used globally in the application;
        - `pages` - Contains the React components and pages used in the application;
        - `domain` - Contains the domain classes used in the application;
        - `services` - Contains the services used in the application, the media types used and the input and output
          models;
            - `App.js` - The main component of the application;
            - `index.js` - The entry point of the application;

In the `js` folder, there are other files used for the development of the application; that are equally relevant to
mention:

- `package.json` - Contains the dependencies of the application;
- `webpack.config.js` - Contains the configuration of the Webpack bundler;
- `tsconfig.json` - Contains the configuration of the TypeScript compiler;
- `eslintrc.json` - Contains the configuration of the ESLint linter;

---

## API

### Connection

To abstract the API connection, the [apiConnection](./src/api/apiConnection.ts) module was implemented and exposes all
HTTP methods supported
by the API, such as `get`, `post`, `put` and `delete`, using a generic `fetchAPI` method.

```typescript
export type ApiResponse<T> = {
  contentType: string;
  json: T;
}

async function fetchApi<T>(path: string, options: Options): Promise<ApiResponse<T>> {
  // ...
}
```

The media types used in the communication with the API are the following:

- `application/json` - Used in the request bodies;
- `application/problem+json` - Used in the response bodies when an error occurs;
- `application/vnd.siren+json` - Used in the response bodies when the request is successful.

### Service

To abstract an API service, the [apiService](./src/api/apiService.ts) module was implemented that exposes a
generic `callApi` method that
receives the HTTP method, the URI and the optional request body and returns a `Promise` with the response.

```typescript
export async function callApi<B, T>(uri: string, method: Method, body?: B)
  : Promise<ApiResponse<T | ProblemModel>> {
  // ...
}
```

Service implementations can be found in the [services](./src/services) folder.

Currently, the following services are implemented:

- [UserService](./src/services/userServices.ts) - Services for user-related operations;
- [GameService](./src/services/gameServices.ts) - Services for game-related operations;
- [SystemService](./src/services/systemServices.ts) - Services for system-related operations;

### Recipes

The [apiRecipes](./src/api/apiRecipes.ts) module provides functions
for obtaining URI templates corresponding to all API resources exposed by the backend.
These templates can be utilized to construct the actual URIs.

This module was developed to address requests related to [Deep Linking](https://en.wikipedia.org/wiki/Deep_linking),
enabling users to access specific resources directly without navigating through the application,
thanks to prior bookmarking, sharing the link with other users, or reloading the page.

Given that the application operates as a Single Page Application (SPA), conventional methods described above would lead
to a 404 error since the server lacks the information on how to handle such requests explicitly.

By utilizing these URI templates, the application can dynamically populate these URIs and seamlessly navigate to the
desired
resource without requiring users to navigate through the entire application.

If the resource requires authentication, the application will redirect the user to the login page and, after a
successful
authentication, will redirect the user to the desired resource.
If the resource is not found, the application will redirect the user to the home page.

## React Context

The [React Context](https://react.dev/learn/typescript#typing-usecontext) is used to share data between components
without having to
pass properties through all of them in a given subtree of React nodes.

The context is used to share the following data:

- User logged-in information, that can be accessed through the `useCurrentUser` and `useSetUser` hooks, to consult and
  update the user information, respectively; Such hooks are used in the beginning of the application to check if the
  user is logged-in and update the user information when the user logs-in or logs-out;

## Webpack Configuration

To establish communication with the backend API, the webpack dev server has been set up to route all requests through a
proxy to the backend API.
This configuration helps circumvent CORS issues and requires no additional configuration on the backend side.

The fallback page is set to the `index.html` file, which is the entry point of the application.

Details mentioned above can be seen in the [webpack.config.js](./webpack.config.js) file.

## Authentication

The user authentication is done in the `Login` or `Register` pages.

The backend API sets a cookie when the user is successfully authenticated,
and that cookie is sent in all subsequent requests by the browser.

When logged-in, another cookie representing the user information is set in the browser.
When logging out, the cookie will present an expired token.

## Tests

The frontend tests use the [Playwright](https://playwright.dev/) framework to test the application in a browser.

Playwright is a powerful, open-source framework developed by Microsoft that enables developers to automate browser tasks
with ease.
It supports all modern web browsers including Chromium, Firefox, and WebKit, and allows for testing on
multiple browser types with a single API.

The tests are located in the [tests](./tests) folder and can be run with the following command inside
the [js](./) folder:

```shell
npm run test
```

Inside the tests folder we have the following files:
 - [`homePageTest.spec.ts`](./tests/homePageTest.spec.ts) - Contains the tests for the home page;
 - [`loginPageTest.spec.ts`](./tests/loginPageTest.spec.ts) - Contains the tests for the login page;
 - [`mePageTest.spec.ts`](./tests/mePageTest.spec.ts) - Contains the tests for the Me page;
 - [`variantsPageTest.spec.ts`](./tests/variantsPageTest.spec.ts) - Contains the tests for the variants' page;

Sadly, we did not have enough time to implement more tests. We plan to implement more tests for the other pages like
we say in the [Further Improvements](#further-improvements) section.

### Implementation Challenges

- **API Integration** - The API integration was a challenge because the API was not fully implemented, at least the way
  we wanted it to be, and the documentation was updated regularly.
  We had to make some changes to the API, particularly using the siren media type, and integrate it with the frontend,
  was not an easy task.

- **Concurrency** - The concurrency was a challenge because we had to deal with multiple asynchronous operations, such
  the initial call to the API to obtain the uri templates. We had to do a mechanism to wait for the response of the API
  and then continue the execution of the application. We resolved this issue by using the `Promise` class that was not
  easy to understand at first, but we managed to solve the problem.

### Further Improvements

- **Add css** - We planned to add css to the application, but we did not have enough time to do it.
- **Add more tests** - We only implemented the basic tests for the application, but we could add more tests to improve
  the code coverage and ensure that the application is working as expected in all possible scenarios.
- **Improve user experience** - We could improve the user experience by adding more features to the application, such as
  notifications, animations, skeleton loading, etc.
- **Add more features and pages** - We could add more features and pages to the application.