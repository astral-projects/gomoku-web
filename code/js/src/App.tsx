import * as React from "react";
import { Outlet, RouterProvider, createBrowserRouter } from "react-router-dom";
import { GomokuContainer } from "./gomokuContainer/GomokuContainer";
import { Home } from "./home/Home";
import { Login } from "./login/Login";
import { Me } from "./me/Me";
import { Register } from "./register/Register";
import { Rankings } from "./Rankings/Rankings";
import { FindGame } from "./findGame/FindGame";
import { Game } from "./game/Game";

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
      {
        path: "/me",
        element: <Me />,
      },
      {
        path:"/register",
        element: <Register />,
      },
      {
        path: "/rankings",
        element: <Rankings />,
      },
      {
        path: "/game",
        element: <FindGame />,
      },
      {
        path: "/game/:gameId",
        element: <Game />,
      },
      {
        path: "/logout",
        element: <Home />,
      }
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
