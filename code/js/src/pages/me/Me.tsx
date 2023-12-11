import * as React from 'react';
import {Link, Navigate, useLocation} from 'react-router-dom';
import {useCurrentUser, useSetUser} from '../GomokuContainer';
import {me} from '../../services/usersServices';
import {HomeOutput} from '../../services/models/users/HomeOutputModel';
import {ProblemModel} from '../../services/media/ProblemModel';
import {isSuccessful} from '../utils/responseData';
import {Email, Id, Username} from '../../domain/User';
import {logUnexpectedAction} from '../utils/logUnexpetedAction';
import {Button} from '@mui/material';

type State =
  | { tag: 'loading' }
  | { tag: 'idle'; button: string; user: string }
  | { tag: 'redirect' }
  | { tag: 'notLoggedIn' };

type Action = { type: 'play' } | { type: 'error'; message: string } | { type: 'success'; user: string };

function reduce(state: State, action: Action): State {
  switch (state.tag) {
    case 'loading':
      if (action.type === 'success') {
        return { tag: 'idle', button: 'Find Match', user: action.user };
      } else if (action.type === 'error') {
        return { tag: 'notLoggedIn' };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'idle':
      if (action.type === 'play') {
        return { tag: 'redirect' };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

export function Me() {
  const [state, dispatch] = React.useReducer(reduce, { tag: 'idle', button: 'Find Match', user: '' });
  const user = useCurrentUser();
  const setUser = useSetUser();
  const location = useLocation();

  React.useEffect(() => {
    if (!user) {
      if (state.tag !== 'idle') {
        return;
      }

      me()
        .then(result => {
          const errorData = result.json as ProblemModel;
          const SuccessData = result.json as unknown as HomeOutput;
          if (!isSuccessful(result.contentType)) {
            console.log(`Error: ${errorData.detail}`);
            dispatch({ type: 'error', message: errorData.detail });
          } else {
            console.log(`Success: ${SuccessData.properties}`);
            const id = SuccessData.properties.id as unknown as Id;
            const username = SuccessData.properties.username as unknown as Username;
            const email = SuccessData.properties.email as unknown as Email;
            setUser({ id: id.value, username: username.value, email: email.value });
            dispatch({ type: 'success', user: username.value });
          }
        })
        .catch(error => {
          console.log(`Error: ${error}`);
          dispatch({ type: 'error', message: error });
        });
    }
  }, [state, setUser, user]);

  if (state.tag === 'notLoggedIn') {
    return <Navigate to={location.state?.source?.pathname || '/login'} replace={true} />;
  }

  if (state.tag === 'redirect') {
    return <Navigate to={location.state?.source?.pathname || '/games'} replace={true} />;
  }

  function onClick(ev: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
    ev.preventDefault();
    if (state.tag !== 'idle') {
      return;
    }
    dispatch({ type: 'play' });
  }

  return (
    <div>
      <fieldset disabled={state.tag !== 'idle'}>
        <p>
          Hello {user?.username}! <Link to={'/logout'}>Logout</Link>
        </p>
        <div>
          <Button onClick={onClick}>{state.tag === 'idle' ? state.button : 'Loading'}</Button>
        </div>
      </fieldset>
    </div>
  );
}
