import * as React from "react";
import { Navigate } from 'react-router-dom';

type State =
    | { tag: 'searching' }
    | { tag: 'found' }
    | { tag: 'error'; message: string }
    | { tag: 'in-lobby' }
    | { tag: 'in-game' };

type Action =
    | { type: 'find' }
    | { type: 'found' }
    | { type: 'error'; message: string }
    | { type: 'join-lobby' }
    | { type: 'start-game' };

function findGameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'find':
            return { tag: 'searching' };
        case 'found':
            return { tag: 'found' };
        case 'error':
            return { tag: 'error', message: action.message };
        case 'join-lobby':
            return { tag: 'in-lobby' };
        case 'start-game':
            return { tag: 'in-game' };
        default:
            return state; 
    }
}

export function FindGame() {
    const [state, dispatch] = React.useReducer(findGameReducer, { tag: 'searching' });

    function delay(delayInMs: number) {
        return new Promise(resolve => setTimeout(resolve, delayInMs));
    }

    React.useEffect(() => {
        delay(5000).then(() => {
            dispatch({ type: 'join-lobby' });
        });
    }, []); 
    
    React.useEffect(() => {
        let isCancelled = false;

        if (state.tag === 'in-lobby') {
            delay(5000).then(() => {
                if (!isCancelled) {
                    dispatch({ type: 'start-game' });
                }
            });
        }

        return () => {
            isCancelled = true;
        };
    }, [state.tag]);

    const leaveLobby = () => {
        dispatch({ type: 'error', message: 'Left the lobby' });
    };

    switch (state.tag) {
        case 'searching':
            return (<div>Searching for a game...</div>);

        case 'in-lobby':
            return (
                <div>
                    In lobby...
                    <button onClick={leaveLobby}>Leave Lobby</button>
                </div>
            );
        case 'in-game': {
            const gameId = 'algumIdDeJogo';
            return <Navigate to={`/game/${gameId}`} replace />;
        }

        case 'error':
            return (
                <div>
                    {state.message} <button onClick={() => dispatch({ type: 'find' })}>Try again</button>
                </div>
            );

        default:
            return (<div>Unexpected state</div>);
    }
}

