import * as React from 'react';
import {Navigate, useLocation} from 'react-router-dom';
import {useSetUser} from '../GomokuContainer';
import {ProblemModel} from '../../services/media/ProblemModel';
import {isSuccessful} from '../utils/responseData';
import {logUnexpectedAction} from '../utils/logUnexpetedAction';
import {logout} from '../../services/usersServices';

type State = { tag: 'loading' } | { tag: 'redirect' } | { tag: 'notLoggedIn' };

type Action = { type: 'error'; message: string } | { type: 'success' };

function reduce(state: State, action: Action): State {
    switch (state.tag) {
        case 'loading':
            if (action.type === 'success') {
                return { tag: 'redirect' };
            } else if (action.type === 'error') {
                return { tag: 'notLoggedIn' };
            } else {
                logUnexpectedAction(state, action);
                return state;
            }

        case 'redirect':
    }
}

export function Logout() {
    const [state, dispatch] = React.useReducer(reduce, { tag: 'loading' });
    const setUser = useSetUser();
    const location = useLocation();

    React.useEffect(() => {
        logout()
            .then(result => {
                if (!isSuccessful(result.contentType)) {
                    const errorData = result.json as ProblemModel;
                    dispatch({ type: 'error', message: errorData.detail });
                } else {
                    setUser(undefined);
                    dispatch({ type: 'success' });
                }
            })
            .catch(error => {
                console.log(`Error: ${error}`);
                dispatch({ type: 'error', message: error });
            });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    switch (state.tag) {
        case 'redirect':
            return <Navigate to={location.state?.source?.pathname || '/'} replace={true} />;
        case 'notLoggedIn':
            return <Navigate to={location.state?.source?.pathname || '/login'} replace={true} />;
        case 'loading':
            return <div>Loading...</div>;
    }
}
