import * as React from 'react';
import { Link } from 'react-router-dom';
import { webRoutes } from '../../App';

export function LoggedHome() {
    return (
        <div>
            <h1>Home</h1>
            <p>Welcome to the Gomoku game!</p>
            <p>
                <Link to={webRoutes.rankings}>
                    <button>Rankings</button>
                </Link>
            </p>
            <p>
                <Link to={webRoutes.games}>
                    <button>Find Game</button>
                </Link>
            </p>
            <p>
                <Link to={webRoutes.me}>
                    <button>My Stats</button>
                </Link>
            </p>
            <p>
                <Link to={webRoutes.logout}>
                    <button>Logout</button>
                </Link>
            </p>
            <p>
                <Link to={webRoutes.about}>
                    <button>About</button>
                </Link>
            </p>
        </div>
    );
}