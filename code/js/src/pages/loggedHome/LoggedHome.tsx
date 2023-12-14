import * as React from 'react';
import { Link } from 'react-router-dom';

export function LoggedHome() {
    return (
        <div>
            <h1>Home</h1>
            <p>Welcome to the Gomoku game!</p>
            <p>
                <Link to="/rankings">
                    <button>Rankings</button>
                </Link>
            </p>
            <p>
                <Link to="/games">
                    <button>Find Game</button>
                </Link>
            </p>
            <p>
                <Link to="/me">
                    <button>My Stats</button>
                </Link>
            </p>
            <p>
                <Link to="/logout">
                    <button>Logout</button>
                </Link>
            </p>
            <p>
                <Link to="/about">
                    <button>About</button>
                </Link>
            </p>
        </div>
    );
}