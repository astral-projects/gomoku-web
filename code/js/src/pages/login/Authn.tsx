import * as React from 'react';
import { useState, createContext, useContext } from 'react';
import { User } from '../../domain/User';

type ContextType = {
  user: User | undefined;
  setUser: (v: User | undefined) => void;
};
const LoggedInContext = createContext<ContextType>({
  user: undefined,
  setUser: () => {},
});

export function AuthnContainer({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState(undefined);
  console.log(`AuthnContainer: ${user}`);
  return <LoggedInContext.Provider value={{ user: user, setUser: setUser }}>{children}</LoggedInContext.Provider>;
}

export function useCurrentUser() {
  return useContext(LoggedInContext).user;
}

export function useSetUser() {
  return useContext(LoggedInContext).setUser;
}
