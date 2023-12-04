import * as React from "react";
import { Link } from "react-router-dom";
import { useCurrentUser } from "../login/Authn";

export function Me() {
  const user = useCurrentUser();
  console.log(user);
  return (
    <div>
      <h1>Me</h1>
      <p>Me</p>
      <p>
        <Link to="/game">
          <button>Search For Opponnet</button>
        </Link>
      </p>
      <p>
        <Link to="/logout">
          <button>Logout</button>
        </Link>
      </p>
    </div>
  );
}