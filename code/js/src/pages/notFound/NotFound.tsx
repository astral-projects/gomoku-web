import * as React from 'react';
import { webRoutes } from '../../App';
import { Link } from 'react-router-dom';
import { Box } from '@mui/material';

export function NotFound() {
    return (
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
            <h1>404</h1>
            <h3>Page not found</h3>
            <div>
                <Box>
                    <Link to={webRoutes.home}>Go Back Home</Link>
                </Box>
            </div>
        </div>
    );
}
