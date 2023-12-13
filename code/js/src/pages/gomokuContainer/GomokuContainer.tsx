import * as React from 'react';
import { createContext, useContext, useState } from 'react';
import { home } from '../../index';
import { HomeOutput } from '../../services/models/users/HomeOutputModel';
import { Email, Id, Username } from '../../domain/User';

type UserInfo = {
    id: number;
    username: string;
    email: string;
};

type ContextType = {
    user: UserInfo | undefined;
    setUser: (v: UserInfo | undefined) => void;
    gameId: number | undefined;
    setGameId: (v: number | undefined) => void;
};

const GomokuContext = createContext<ContextType>({
    user: undefined,
    setUser: () => {},
    gameId: undefined,
    setGameId: () => {},
});

export function GomokuContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState(undefined);
    const [gameId, setGameId] = useState(undefined);
    React.useEffect(() => {
        home.then(response => {
            if (response === null) {
                return;
            }
            const SuccessData = response as unknown as HomeOutput;
            const id = SuccessData.properties.id as unknown as Id;
            const username = SuccessData.properties.username as unknown as Username;
            const email = SuccessData.properties.email as unknown as Email;
            setUser({ id: id.value, username: username.value, email: email.value });
        }).catch(() => {
            console.log('Error fetching home');
        });
    }, []);

    if (user !== undefined) {
        console.log(`Logged in as ${user.username}`);
        console.log(user);
    } else {
        console.log('Not logged in');
    }

    return (
        <GomokuContext.Provider value={{ user: user, setUser: setUser, gameId: gameId, setGameId: setGameId }}>
            {children}
        </GomokuContext.Provider>
    );
}

export function useCurrentUser() {
    return useContext(GomokuContext).user;
}

export function useCurrentGameId() {
    return useContext(GomokuContext).gameId;
}

export function useSetGameId() {
    return useContext(GomokuContext).setGameId;
}

export function useSetUser() {
    return useContext(GomokuContext).setUser;
}
