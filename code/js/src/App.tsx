import * as React from "react";
import { Outlet, RouterProvider, createBrowserRouter } from "react-router-dom";
import { GomokuContainer } from "./gomokuContainer/GomokuContainer";
import { Home } from "./home/Home";
import { Login } from "./login/Login";

const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <GomokuContainer>
        <Outlet />
      </GomokuContainer>
    ),
    children: [
      {
        path: "/",
        element: <Home />,
      },
      {
        path: "/login",
        element: <Login />,
      },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
