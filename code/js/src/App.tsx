import * as React from 'react';
import {createBrowserRouter, Outlet, RouterProvider} from 'react-router-dom';
import {GomokuContainer} from './pages/gomokuContainer/GomokuContainer';
import {Home} from './pages/home/Home';
import {Login} from './pages/login/Login';
import {Me} from './pages/me/Me';
import {Error} from './pages/error/Error';
import {Register} from './pages/register/Register';
import {Rankings} from './pages/Rankings/Rankings';
import {FindGame} from './pages/findGame/FindGame';
import {Game} from './pages/game/Game';
import {About} from './pages/about/About';
import {Logout} from './pages/logout/Logout';
import {UserStats} from './pages/userstats/UserStats';

/*export const routes = {
    home: '/',
    login: '/login',
    me: '/me',
    register: '/register',
    rankings: '/rankings',
    game: '/games',
    logout: '/logout',
    error: '/error',
};
*/
const router = createBrowserRouter([
    {
        path: '/',
        element: (
            <GomokuContainer>
                <Outlet />
            </GomokuContainer>
        ),
        children: [
            {
                path: '/',
                element: <Home />,
            },
            {
                path: '/login',
                element: <Login />,
            },
            {
                path: '/me',
                element: <Me />,
            },
            {
                path: '/register',
                element: <Register />,
            },
            {
                path: '/rankings',
                element: <Rankings />,
            },
            {
                path: '/rankings/:id',
                element: <UserStats />,
            },
            {
                path: '/games',
                element: <FindGame />,
            },
            {
                path: '/games/:gameId',
                element: <Game />,
            },
            {
                path: '/logout',
                element: <Logout />,
            },
            {
                path: '/about',
                element: <About/>,
            },
            {
                path: '/error',
                element: <Error />,
            },
        ],
    },
]);

export function App() {
    return <RouterProvider router={router} />;
}
