import * as React from "react";
import { Link } from "react-router-dom";

export function Home() {
  return (
    <div>
      <h1>Home</h1>
      <p>Welcome to the Gomoku game!</p>
      <p> 
        <Link to= "/rankigs">
          <button>Rankings</button>
        </Link>
      </p>
      <p>
        <Link to="/login">
          <button>Login</button>
        </Link>
      </p>
      <p>
        <Link to="/register">
          <button>Create Account</button>
        </Link>
      </p>
      <p>
        <Link to="/about">
          <button>About</button>
        </Link>
      </p>
    </div>
  );
}
