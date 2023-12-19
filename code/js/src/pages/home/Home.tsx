import * as React from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUserId } from '../GomokuContainer';
import { Navigate } from 'react-router-dom';
import { webRoutes } from '../../App';

export function Home() {
    const user = useCurrentUserId();
    return (
        <div>
            {user ? <Navigate to={webRoutes.me} /> : null}
            <h1>Home</h1>
            <p>Welcome to the Gomoku game!</p>
            <ul>
                <li>
                    <Link to={webRoutes.login}>Login</Link>
                </li>
                <li>
                    <Link to={webRoutes.register}>Sign up</Link>
                </li>
            </ul>
        </div>
    );
}
