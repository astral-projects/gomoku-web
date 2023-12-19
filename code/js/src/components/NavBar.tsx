import * as React from 'react';
import { useCurrentUserName } from '../pages/GomokuContainer';
import { Link } from 'react-router-dom';
import { webRoutes } from '../App';

export function Navbar() {
    const user = useCurrentUserName();
    return (
        <nav>
            <ul style={{ listStyleType: 'none', display: 'flex', justifyContent: 'space-around' }}>
                <li>
                    {user ? (
                        <Link to={`${webRoutes.logout}`}>Logout</Link>
                    ) : (
                        <Link to={`${webRoutes.login}`}>Login</Link>
                    )}
                </li>
                <li>{user ? <Link to={`${webRoutes.me}`}>Home</Link> : <Link to={`${webRoutes.home}`}>Home</Link>}</li>
                <li>
                    <Link to={`${webRoutes.rankings}`}>Rankings</Link>
                </li>
                <li>
                    <Link to={`${webRoutes.about}`}>About</Link>
                </li>
            </ul>
        </nav>
    );
}
