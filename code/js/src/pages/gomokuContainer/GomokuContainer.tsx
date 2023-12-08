import * as React from 'react';
import { createContext, useContext, useState } from 'react';

type UserInfo = {
  id: number;
  username: string;
  email: string;
};

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
  console.log(`AuthnContainer: ${user}`);
  return <GomokuContext.Provider value={{ user: user, setUser: setUser }}>{children}</GomokuContext.Provider>;
  
}

export function useCurrentUser() {
  return useContext(GomokuContext).user;
}

export function useSetUser() {
  return useContext(GomokuContext).setUser;
}