import * as React from 'react';
import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSetUser } from './Authn';
import { login } from '../../services/usersServices';
import { useCookies } from 'react-cookie';

type State =
  | { tag: 'editing'; error?: string; inputs: { username: string; password: string } }
  | { tag: 'submitting'; username: string; password: string }
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
        return {
          tag: 'editing',
          error: undefined,
          inputs: { ...state.inputs, [action.inputName]: action.inputValue },
        };
      } else if (action.type === 'submit') {
        return { tag: 'submitting', username: state.inputs.username, password: state.inputs.password };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { tag: 'redirect' };
      } else if (action.type === 'error') {
        return { tag: 'editing', error: action.message, inputs: { username: state.username, password: '' } };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

export function Login() {
  const [state, dispatch] = React.useReducer(reduce, { tag: 'editing', inputs: { username: '', password: '' } });
  const setUser = useSetUser();
  const location = useLocation();
  const [cookies, setCookie] = useCookies(['token']);

  useEffect(() => {
    if (state.tag !== 'submitting') {
      return;
    }
    login(state.username, state.password)
      .then(res => {
        // TODO: How to avoid cors without load balancer in a simple way?
        console.log(res);
        console.log('Login success');
        setCookie('token', res.data.token, { httpOnly: true, sameSite: 'strict', secure: true });
        setUser(res.data);
        dispatch({ type: 'success' });
      })
      .catch(err => {
        console.log('Login error');
        dispatch({ type: 'error', message: err.message });
      });
  });

  if (state.tag === 'redirect') {
    return <Navigate to={location.state?.source?.pathname || '/me'} replace={true} />;
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
  }

  const username = state.tag === 'submitting' ? state.username : state.inputs.username;
  const password = state.tag === 'submitting' ? '' : state.inputs.password;
  return (
    <form onSubmit={handleSubmit}>
      {state.tag === 'submitting' && cookies.token}
      <fieldset disabled={state.tag !== 'editing'}>
        <div>
          <label htmlFor="username">Username</label>
          <input id="username" type="text" name="username" value={username} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input id="password" type="text" name="password" value={password} onChange={handleChange} />
        </div>
        <div>
          <button type="submit">Login</button>
        </div>
      </fieldset>
      {state.tag === 'editing' && state.error}
    </form>
  );
}
