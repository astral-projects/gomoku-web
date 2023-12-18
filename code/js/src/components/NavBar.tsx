import * as React from 'react';
import { useCurrentUserName } from '../pages/GomokuContainer';
import { Link } from 'react-router-dom';

export function Navbar() {
    const user = useCurrentUserName();
    return (
        <nav>
            <ul style={{ listStyleType: 'none', display: 'flex', justifyContent: 'space-around' }}>
                <li>{user ? <Link to="/logout">Logout</Link> : <Link to="/login">Login</Link>}</li>
                <li>{user ? <Link to="/me">Home</Link> : <Link to="/">Home</Link>}</li>
                <li>
                    <Link to="/rankings">Rankings</Link>
                </li>
                <li>
                    <Link to="/about">About</Link>
                </li>
            </ul>
        </nav>
    );
}
