import * as React from 'react';
import { Navigate } from 'react-router-dom';
import { getCookie } from '../pages/utils/getCookies';

export function RequireAuthn({ children }: { children: React.ReactNode }): React.ReactElement {
    const cookie = getCookie('_user');
    if (cookie) {
        console.log('logged in');
        return <>{children}</>;
    } else {
        console.log('not logged in on auth container');
        return <Navigate to="/login" />;
    }
}
