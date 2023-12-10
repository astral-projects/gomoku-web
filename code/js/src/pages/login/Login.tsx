import * as React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { login } from '../../services/usersServices';
import { ProblemModel } from '../../services/media/ProblemModel';
import { LoginOutput } from '../../services/models/users/LoginOuputModel';
import { Email, Id, User, Username } from '../../domain/User';
import { Entity } from '../../services/media/siren/Entity';
import { useSetUser } from '../gomokuContainer/GomokuContainer';
import { logUnexpectedAction } from '../utils/logUnexpetedAction';
import { isSuccessful } from '../utils/responseData';

type State =
  | { tag: 'editing'; error?: string; inputs: { username: string; password: string } }
  | { tag: 'submitting'; username: string; password: string }
  | { tag: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; message: string }
  | { type: 'success' };

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
    login({ username: state.inputs.username, password: state.inputs.password })
      .then(result => {
        if (!isSuccessful(result.contentType)) {
          const errorData = result.json as ProblemModel;
          dispatch({ type: 'error', message: errorData.detail });
        } else {
          const successData = result.json as LoginOutput;
          const properties = successData.entities[0] as Entity<User>;
          const id = properties.properties.id as Id;
          const username = properties.properties.username as Username;
          const email = properties.properties.email as Email;
          setUser({ id: id.value, username: username.value, email: email.value });
          dispatch({ type: 'success' });
        }
      })
      .catch((err: { message: string }) => {
        dispatch({ type: 'error', message: err.message });
      });
  }

  const username = state.tag === 'submitting' ? state.username : state.inputs.username;
  const password = state.tag === 'submitting' ? '' : state.inputs.password;
  return (
    <div>
      <form onSubmit={handleSubmit}>
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
    </div>
  );
}
