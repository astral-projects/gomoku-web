import * as React from 'react';
import { createContext, useContext, useState } from 'react';
import { UserInfo } from '../domain/UserInfo';
import { home } from '../index';
import { HomeOutput } from '../services/models/users/HomeOutputModel';
import { Email, Id, Username } from '../domain/User';

type ContextType = {
    user: UserInfo | undefined;
    setUser: (v: UserInfo | undefined) => void;
};

const GomokuContext = createContext<ContextType>({
    user: undefined,
    setUser: () => {},
});

export function GomokuContainer({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState(undefined);
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

    return <GomokuContext.Provider value={{ user: user, setUser: setUser }}>{children}</GomokuContext.Provider>;
}

export function useCurrentUser() {
    return useContext(GomokuContext).user;
}

export function useSetUser() {
    return useContext(GomokuContext).setUser;
}
