import * as React from "react";
import { Link } from "react-router-dom";

export function Me() {
  return (
    <div>
      <h1>Me</h1>
      <p>Me</p>
      <p>
        <Link to="/findGame">
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