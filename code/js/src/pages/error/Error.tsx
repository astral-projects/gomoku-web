import * as React from 'react';
import { Link } from 'react-router-dom';
import { webRoutes } from '../../App';

export function Error() {
    return (
        <div>
            <h1>Error</h1>
            <p>Something went wrong. Please try again later.</p>
            <h2>
                Go back <Link to={`${webRoutes.error}`}>home</Link>
            </h2>
        </div>
    );
}
