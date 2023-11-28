import * as React from "react";
import { Link } from "react-router-dom";

export function Home() {
  return (
    <div>
      <h1>Home</h1>
      <p>Welcome to the Gomoku game!</p>
      <p>
        Please <Link to="/login">Login</Link> to play.
      </p>
    </div>
  );
}
