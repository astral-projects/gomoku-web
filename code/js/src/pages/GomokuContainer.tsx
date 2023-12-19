import * as React from 'react';
import { createContext, useContext, useEffect, useState } from 'react';
import { getCookie } from '../pages/utils/getCookies';
import { Navbar } from '../components/NavBar';

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

export function GomokuContainer({ children }: { children: React.ReactNode }) {
    const [userName, setUserName] = useState(undefined);
    const [userId, setUserId] = useState(undefined);

    useEffect(() => {
        const cookieName = getCookie('_user');
        if (cookieName) {
            const value = cookieName.split(',');
            setUserName(value[0]);
            setUserId(parseInt(value[1]));
        }
    }, []);

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
