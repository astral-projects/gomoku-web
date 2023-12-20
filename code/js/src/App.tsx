import * as React from 'react';
import { createBrowserRouter, Outlet, RouterProvider } from 'react-router-dom';
import { GomokuContainer } from './pages/GomokuContainer';
import { Home } from './pages/home/Home';
import { Login } from './pages/login/Login';
import { Me } from './pages/me/Me';
import { Error } from './pages/error/Error';
import { Register } from './pages/register/Register';
import { FindGame } from './pages/findGame/FindGame';
import { Game } from './pages/game/Game';
import { About } from './pages/about/About';
import { Logout } from './pages/logout/Logout';
import { UserStats } from './pages/userstats/UserStats';
import { RequireAuthn } from './pages/AuthContainer';
import { NotFound } from './pages/notFound/NotFound';
import { Rankings } from './pages/rankings/Rankings';
import { Lobby } from './pages/lobby/Lobby';

export const webRoutes = {
    home: '/',
    me: '/me',
    login: '/login',
    register: '/register',
    rankings: '/rankings',
    games: '/games',
    logout: '/logout',
    about: '/about',
    error: '/error',
    userStats: '/rankings/:id',
    game: '/games/:gameId',
    lobby: '/lobby/:lobbyId',
};

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
                path: webRoutes.home,
                element: <Home />,
            },
            {
                path: webRoutes.login,
                element: <Login />,
            },
            {
                path: webRoutes.me,
                element: (
                    <RequireAuthn>
                        <Me />,
                    </RequireAuthn>
                ),
            },
            {
                path: webRoutes.register,
                element: <Register />,
            },
            {
                path: webRoutes.rankings,
                element: <Rankings />,
            },
            {
                path: webRoutes.userStats,
                element: <UserStats />,
            },
            {
                path: webRoutes.games,
                element: (
                    <RequireAuthn>
                        <FindGame />,
                    </RequireAuthn>
                ),
            },
            {
                path: webRoutes.lobby,
                element: (
                    <RequireAuthn>
                        <Lobby />,
                    </RequireAuthn>
                ),
            },
            {
                path: webRoutes.game,
                element: (
                    <RequireAuthn>
                        <Game />
                    </RequireAuthn>
                ),
            },
            {
                path: webRoutes.logout,
                element: (
                    <RequireAuthn>
                        <Logout />,
                    </RequireAuthn>
                ),
            },
            {
                path: webRoutes.about,
                element: <About />,
            },
            {
                path: webRoutes.error,
                element: <Error />,
            },
            {
                path: '*',
                element: <NotFound />,
            },
        ],
    },
]);

export function App() {
    return <RouterProvider router={router} />;
}
