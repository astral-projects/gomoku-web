import * as React from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useCurrentUser } from '../gomokuContainer/GomokuContainer';
import { logUnexpectedAction } from '../utils/logUnexpetedAction';
import { Button } from '@mui/material';

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
            } else if (action.type === 'error') {
                return { tag: 'notLoggedIn' };
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
    const user = useCurrentUser();
    const [state, dispatch] = React.useReducer(reduce, {
        tag: 'idle',
        button: 'Find Match',
        user: user?.username || '',
    });
    const location = useLocation();

    function onClick(ev: React.MouseEvent<HTMLButtonElement, MouseEvent>) {
        ev.preventDefault();

        if (state.tag === 'loading') {
            return;
        } else if (state.tag === 'notLoggedIn') {
            return;
        }

        dispatch({ type: 'play' });
    }

    if (state.tag === 'redirect') {
        return <Navigate to={location.state?.source?.pathname || '/games'} />;
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
