import * as React from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUserId } from '../GomokuContainer';
import { Navigate } from 'react-router-dom';

export function Home() {
    const user = useCurrentUserId();
    return (
        <div>
            {user ? <Navigate to="/me" /> : null}
            <h1>Home</h1>
            <p>Welcome to the Gomoku game!</p>
            <ul>
                <li>
                    <Link to="/login">Login</Link>
                </li>
                <li>
                    <Link to="/register">Sign up</Link>
                </li>
            </ul>
        </div>
    );
}
