import * as React from 'react';
import { createContext, useContext, useEffect, useState } from 'react';
import { getCookie } from '../pages/utils/getCookies';
import { Link } from 'react-router-dom';

type ContextType = {
    userName: string | undefined;
    setUserName: (v: string | undefined) => void;
    userId: number | undefined;
    setUserId: (v: number | undefined) => void;
};

const GomokuContext = createContext<ContextType>({
    userName: undefined,
    setUserName: () => {},
    userId: undefined,
    setUserId: () => {},
});

const Navbar = () => {
    return (
        <nav>
            <ul style={{ listStyleType: 'none', display: 'flex', justifyContent: 'space-around' }}>
                <li>
                    <Link to="/">Home</Link>
                </li>
                <li>
                    <Link to="/rankings">Rankings</Link>
                </li>
                <li>
                    <Link to="/about">About</Link>
                </li>
                <li>
                    <Link to="/logout">Logout</Link>
                </li>
            </ul>
        </nav>
    );
};

export function GomokuContainer({ children }: { children: React.ReactNode }) {
    const [userName, setUserName] = useState(undefined);
    const [userId, setUserId] = useState(undefined);

    useEffect(() => {
        const cookieName = getCookie('user_name');
        if (cookieName) {
            setUserName(cookieName);
        }
        const cookieId = getCookie('user_id');
        if (cookieId) {
            setUserId(Number(cookieId));
        }
    }, []);

    if (userName !== undefined) {
        console.log(`Logged in as ${userName}`);
        console.log(userName);
    } else {
        console.log('Not logged in');
    }

    return (
        <GomokuContext.Provider
            value={{ userName: userName, setUserName: setUserName, userId: userId, setUserId: setUserId }}
        >
            <Navbar />
            {children}
        </GomokuContext.Provider>
    );
}

export function useCurrentUserName() {
    return useContext(GomokuContext).userName;
}

export function useSetUserName() {
    return useContext(GomokuContext).setUserName;
}

export function useCurrentUserId() {
    return useContext(GomokuContext).userId;
}

export function useSetUserId() {
    return useContext(GomokuContext).setUserId;
}
