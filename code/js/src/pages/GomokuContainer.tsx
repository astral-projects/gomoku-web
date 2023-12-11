import * as React from 'react';
import {createContext, useContext, useState} from 'react';
import {UserInfo} from "../domain/UserInfo";

type ContextType = {
    user: UserInfo | undefined;
    setUser: (v: UserInfo | undefined) => void;
    gameId: number | undefined;
    setGameId: (v: number | undefined) => void;
};

const GomokuContext = createContext<ContextType>({
    user: undefined,
    setUser: () => {
    },
    gameId: undefined,
    setGameId: () => {
    },
});

export function GomokuContainer({children}: { children: React.ReactNode }) {
    const [user, setUser] = useState(undefined);
    const [gameId, setGameId] = useState(undefined);
    console.log(`AuthnContainer: ${user}`);
    return <GomokuContext.Provider value={{
        user: user,
        setUser: setUser,
        gameId: gameId,
        setGameId: setGameId
    }}>{children}</GomokuContext.Provider>;

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