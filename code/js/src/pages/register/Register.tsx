import * as React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

type State =
  | { tag: 'editing'; error?: string; inputs: { username: string; email:string, password: string, confirmPassword: string } }
  | { tag: 'submitting'; username: string, email: string}
  | { tag: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; message: string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type} on state '${state.tag}'`);
}

function reduce(state: State, action: Action): State {
  switch (state.tag) {
    case 'editing':
      if (action.type === 'edit') {
        return { tag: 'editing', error: undefined, inputs: { ...state.inputs, [action.inputName]: action.inputValue } };
      } else if (action.type === 'submit') {
        return { tag: 'submitting', username: state.inputs.username, email: state.inputs.email };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { tag: 'redirect' };
      } else if (action.type === 'error') {
        return { tag: 'editing', error: action.message, inputs: { username: state.username, email: state.email, password: '', confirmPassword:'', } };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

function delay(delayInMs: number) {
  return new Promise(resolve => {
    setTimeout(() => resolve(undefined), delayInMs);
  });
}

export async function validateCredentials(username: string, email: string, password: string, confirmPassword:string): Promise<string | undefined> {
  await delay(5000);

  if (username.length < 8) {
    return 'Username must be at least 8 characters long.';
  }

  if (!email.includes('@') || !email.endsWith('.com')) {
    return 'Email must contain "@" and end with ".com".';
  }

  if (password.length < 8) {
    return 'Password must be at least 8 characters long.';
  }

  if (password !== confirmPassword) {
    return 'Password and confirm password must match.';
  }

  return undefined;
}


export function Register() {
  console.log('Register');
  const [state, dispatch] = React.useReducer(reduce, { tag: 'editing', inputs: { username: '',email: '', password: '', confirmPassword:' ' } });
  const location = useLocation();
  if (state.tag === 'redirect') {
    return <Navigate to={location.state?.source?.pathname || '/login'} replace={true} />;
  }
  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
  }
  function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.tag !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });
    const username = state.inputs.username;
    const email = state.inputs.email;
    const password = state.inputs.password;
    const confirmPassword = state.inputs.password;
    validateCredentials(username,email, password, confirmPassword)
      .then(res => {
        if (res===undefined) {
          dispatch({ type: 'success' });
        } else {
          dispatch({ type: 'error', message: res});
        }
      })
      .catch(error => {
        dispatch({ type: 'error', message: error.message });
      });
  }

  const username = state.tag === 'submitting' ? state.username : state.inputs.username
  const email = state.tag === 'submitting' ? state.email : state.inputs.email
  const password = state.tag === 'submitting' ? "" : state.inputs.password
    const confirmPassword = state.tag === 'submitting' ? "" : state.inputs.confirmPassword
  return (
    <form onSubmit={handleSubmit}>
      <fieldset disabled={state.tag !== 'editing'}>
        <div>
          <label htmlFor="username">Username</label>
          <input id="username" type="text" name="username" value={username} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="email">Email</label>
          <input id="email" type="text" name="email" value={email} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input id="password" type="text" name="password" value={password} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="confirmPassword">Confirm Password</label>
          <input id="confirmPassword" type="text" name="confirmPassword" value={confirmPassword} onChange={handleChange} />
        </div>
        <div>
          <button type="submit">Register</button>
        </div>
      </fieldset>
      {state.tag === 'editing' && state.error}
    </form>
  );
}