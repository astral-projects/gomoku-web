import * as React from "react";
import { Link } from "react-router-dom";

export function Home() {
  
  return (
    <div>
      <h1>Home</h1>
      <p>Welcome to the Gomoku game!</p>
      <ul>
        <li>
          <Link to="/login">Login</Link>
        </li>
        <li>
          <Link to="/rankings">Rankings</Link>
        </li>
        <li>
          <Link to="/logout">Logout</Link>
        </li>
        <li>
          <Link to="/about">About</Link>
        </li>
      </ul>
    </div>
  );
}
