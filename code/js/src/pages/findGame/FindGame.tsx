import * as React from 'react';
import { Navigate } from 'react-router-dom';
import { getVariants, waittingInLobby, findGame, exitLobby } from '../../services/gameServices';
import { ProblemModel } from '../../services/media/ProblemModel';
import { FindGameOutput } from '../../services/models/games/FindGameOutputModel';
import { webRoutes } from '../../App';
import { replacePathVariables } from '../utils/replacePathVariables';

/**
 * The state of the component can be in one of the following states:
 */
type State =
    | { tag: 'loading-variants' }
    | { tag: 'found' }
    | { tag: 'error'; message: string }
    | { tag: 'in-lobby'; lobbyId: number }
    | { tag: 'in-game'; gameId: number }
    | { tag: 'selecting-variant' };

/**
 * The action that can be dispatched to the reducer.
 */
type Action =
    | { type: 'find' }
    | { type: 'variants-loaded' }
    | { type: 'join-lobby'; lobbyId: number }
    | { type: 'start-game'; gameId: number }
    | { type: 'select-variant'; variantId: number }
    | { type: 'error'; message: string };

/**
 * This function will update the state based on the action.
 * @param state
 * @param action
 * @returns
 */
function findGameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'find':
            return { tag: 'loading-variants' };
        case 'variants-loaded':
            return { tag: 'selecting-variant' };
        case 'join-lobby':
            return { tag: 'in-lobby', lobbyId: action.lobbyId };
        case 'start-game':
            return { tag: 'in-game', gameId: action.gameId };
        case 'error':
            return { tag: 'error', message: action.message };
        default:
            return state;
    }
}

/**
 * This component will find a game for the user. It will first display a list of available variants. Once the user selects a variant, it will find a game with that variant. If a game is found, it will redirect the user to the game page.
 */
export function FindGame() {
    const [state, dispatch] = React.useReducer(findGameReducer, { tag: 'loading-variants' });
    const [variants, setVariants] = React.useState(null);
    const [isPollingActive, setIsPollingActive] = React.useState(false);

    const startPollingLobbyStatus = React.useCallback(
        lobbyId => {
            setIsPollingActive(true);
            const intervalId = setInterval(() => {
                if (!isPollingActive) return;
                waittingInLobby(lobbyId).then(result => {
                    const errorData = result.json as ProblemModel;
                    const successData = result.json as unknown as FindGameOutput;
                    if (result.contentType === 'application/problem+json') {
                        dispatch({ type: 'error', message: errorData.detail });
                        setIsPollingActive(false);
                    } else if (result.contentType === 'application/vnd.siren+json') {
                        if (successData.class.find(c => c == 'lobby') != undefined) {
                            dispatch({ type: 'join-lobby', lobbyId: successData.properties.id });
                        } else if (successData.class.find(c => c == 'game') != undefined) {
                            const gameId = successData.properties.id;
                            setIsPollingActive(false);
                            dispatch({ type: 'start-game', gameId: gameId });
                        }
                    }
                });
            }, 5000);
            return intervalId;
        },
        [setIsPollingActive, isPollingActive]
    );

    React.useEffect(() => {
        let intervalId;
        if (state.tag === 'loading-variants') {
            getVariants().then(result => {
                const errorData = result.json as ProblemModel;
                const successData = result.json as unknown as FindGameOutput;
                if (result.contentType === 'application/problem+json') {
                    dispatch({ type: 'error', message: errorData.detail });
                } else if (result.contentType === 'application/vnd.siren+json') {
                    setVariants(successData.properties);
                    dispatch({ type: 'variants-loaded' });
                }
            });
        }

        if (state.tag === 'in-lobby') {
            intervalId = startPollingLobbyStatus(state.lobbyId);
        }
        return () => {
            if (intervalId) {
                clearInterval(intervalId);
            }
            setIsPollingActive(false);
        };
    }, [state, startPollingLobbyStatus]);

    switch (state.tag) {
        case 'selecting-variant':
            return (
                <div>
                    Select a variant:
                    <select
                        onChange={e => handleFindGame(parseInt(e.target.value), dispatch, setIsPollingActive)}
                        style={{ display: 'block', margin: '10px 0' }}
                    >
                        <option value=""> -- select an option -- </option>
                        {variants &&
                            variants.map(variant => (
                                <option key={variant.id} value={variant.id.value}>
                                    {variant.name}
                                </option>
                            ))}
                    </select>
                </div>
            );
        case 'loading-variants':
            return <div>Searching for game variants...</div>;

        case 'in-lobby':
            return (
                <div>
                    In lobby...
                    <button onClick={() => handleLeaveLobby(state.lobbyId, dispatch, setIsPollingActive)}>
                        Leave Lobby
                    </button>
                </div>
            );
        case 'in-game': {
            const gameId = state.gameId;
            return <Navigate to={replacePathVariables(webRoutes.game, [gameId])} />;
        }

        case 'error':
            return (
                <div>
                    {state.message}
                    <button onClick={() => dispatch({ type: 'find' })}>Try again</button>
                </div>
            );

        default:
            return <div>Unexpected state</div>;
    }
}

/**
 * Find a game with the given variant id.The function will dispatch the appropriate action based on the result.
 * @param variantId
 * @param dispatch
 * @param setIsPollingActive
 */
function handleFindGame(
    variantId: number,
    dispatch: (action: Action) => void,
    setIsPollingActive: (isPollingActive: boolean) => void
) {
    findGame({ variantId: variantId })
        .then(result => {
            const errorData = result.json as ProblemModel;
            const successData = result.json as unknown as FindGameOutput;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                if (successData.class.find(c => c == 'lobby') != undefined) {
                    dispatch({ type: 'join-lobby', lobbyId: successData.properties.id });
                } else if (successData.class.find(c => c == 'game') != undefined) {
                    const gameId = successData.properties.id;
                    setIsPollingActive(false);
                    dispatch({ type: 'start-game', gameId: gameId });
                }
            }
        })
        .catch((err: { message: string }) => {
            dispatch({ type: 'error', message: err.message });
        });
}

/**
 * This function will exit the lobby with the given id. The function will dispatch the appropriate action based on the result.
 * @param lobbyId
 * @param dispatch
 * @param setIsPollingActive
 */
function handleLeaveLobby(
    lobbyId: number,
    dispatch: (action: Action) => void,
    setIsPollingActive: (isPollingActive: boolean) => void
) {
    exitLobby(lobbyId).then(result => {
        const errorData = result.json as ProblemModel;
        if (result.contentType === 'application/problem+json') {
            dispatch({ type: 'error', message: errorData.detail });
        } else if (result.contentType === 'application/vnd.siren+json') {
            setIsPollingActive(false);
            dispatch({ type: 'variants-loaded' });
        }
    });
}
