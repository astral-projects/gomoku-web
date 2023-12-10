import * as React from "react";
import { Navigate } from 'react-router-dom';
import { getVariants, waittingInLobby, findGame, exitLobby } from "../../services/gameServices";
import { ProblemModel } from '../../services/media/ProblemModel';
import { FindGameOutput } from "../../services/users/models/games/FindGameOutputModel";
import { useSetGameId } from "../gomokuContainer/GomokuContainer";
//import { LobbyOutput } from "../../services/users/models/lobby/LobbyOutputModel";
//localstorage
type State =
    | { tag: 'loading-variants' }
    | { tag: 'found' }
    | { tag: 'error'; message: string }
    | { tag: 'in-lobby', lobbyId: number }
    | { tag: 'in-game'; gameId: number }
    | { tag: 'selecting-variant' };

type Action =
    | { type: 'find' }
    | { type: 'variants-loaded' }
    | { type: 'found' }
    | { type: 'error'; message: string }
    | { type: 'join-lobby', lobbyId: number }
    | { type: 'leave-lobby' }
    | { type: 'start-game', gameId: number }
    | { type: 'select-variant'; variantId: number };

function findGameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'find':
            return { tag: 'loading-variants' };
        case 'found':
            return { tag: 'found' };
        case 'variants-loaded':
            return { tag: 'selecting-variant' };
        case 'error':
            return { tag: 'error', message: action.message };
        case 'join-lobby':
            return { tag: 'in-lobby', lobbyId: action.lobbyId };
        case 'leave-lobby':
            return { tag: 'selecting-variant' };
        case 'start-game':
            console.log('start game aqewqeqwe');
            return { tag: 'in-game', gameId: action.gameId };
        default:
            return state;
    }
}



export function FindGame() {
    const [state, dispatch] = React.useReducer(findGameReducer, { tag: 'loading-variants' });
    const [variants, setVariants] = React.useState(null);
    const setGameId = useSetGameId();
    const pollingIntervalRef = React.useRef(null);

    const fetchGame = (variantId) => {
        findGame({ variantId: variantId }).then(result => {
            console.log(`Nao acredito que ele me faz isto fds`);
            const errorData = result.json as ProblemModel;
            const successData = result.json as unknown as FindGameOutput;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                if (successData.class.find((c) => c == 'lobby') != undefined) {
                    dispatch({ type: 'join-lobby', lobbyId: successData.properties.id });
                } else if (successData.class.find((c) => c == 'game') != undefined) {
                    const gameId = successData.properties.id;
                    setGameId(gameId);
                    dispatch({ type: 'start-game', gameId: gameId });
                }
            }
        })
            .catch((err: { message: string }) => {
                dispatch({ type: 'error', message: err.message });
            });

    };



    const fetchVariants = () => {
        getVariants().then(result => {
            const errorData = result.json as ProblemModel;
            const successData = result.json as unknown as FindGameOutput;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                setVariants(successData.properties);
                localStorage.setItem('gameVariants', JSON.stringify(successData.properties));
                dispatch({ type: 'variants-loaded' });
            }
        })
    };

    const stopPollingLobbyStatus = React.useCallback(() => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    }, []);

    const pollLobbyStatus = React.useCallback((lobbyId) => {
        waittingInLobby(lobbyId).then(result => {
            const errorData = result.json as ProblemModel;
            const successData = result.json as unknown as FindGameOutput;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
                stopPollingLobbyStatus();
            } else if (result.contentType === 'application/vnd.siren+json') {
                if (successData.class.find((c) => c == 'lobby') != undefined) {
                    dispatch({ type: 'join-lobby', lobbyId: successData.properties.id });
                } else if (successData.class.find((c) => c == 'game') != undefined) {
                    const gameId = successData.properties.id;
                    setGameId(gameId);
                    dispatch({ type: 'start-game', gameId: gameId });
                }
            }

        })
           // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const startPollingLobbyStatus = React.useCallback((lobbyId) => {
        stopPollingLobbyStatus(); 
        pollingIntervalRef.current = setInterval(() => {
            pollLobbyStatus(lobbyId);
        }, 5000);
    }, [pollLobbyStatus, stopPollingLobbyStatus]);




    React.useEffect(() => {
        if (state.tag === 'loading-variants') {
            fetchVariants();
        }
        if (state.tag === 'in-lobby') {
            console.log('start polling lobby status');
            startPollingLobbyStatus(state.lobbyId);
        } 

        return () => {
            console.log('stop polling lobby status in the return');
            stopPollingLobbyStatus();
        };
    }, [state, startPollingLobbyStatus, stopPollingLobbyStatus, pollLobbyStatus]);

    const handleLeaveLobby = (lobbyId) => {
        exitLobby(lobbyId).then(result => {
            const errorData = result.json as ProblemModel;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                stopPollingLobbyStatus();
                dispatch({ type: 'leave-lobby' });
            }
        })
    };



    switch (state.tag) {
        case 'selecting-variant':
            return (
                <div>
                    Select a variant:
                    {variants && variants.map(variant => (
                        <button
                            key={variant.id}
                            style={{ display: 'block', margin: '10px 0' }}
                            onClick={() => fetchGame(variant.id.value)}
                        >
                            {variant.name}
                        </button>
                    ))}
                </div>
            );
        case 'loading-variants':
            return <div>Searching for game variants...</div>;

        case 'in-lobby':
            return (
                <div>
                    In lobby...
                    <button onClick={() => handleLeaveLobby(state.lobbyId)}>Leave Lobby</button>
                </div>
            );
        case 'in-game': {
            const gameId = state.gameId;
            console.log('passou aquio fodsaew');
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

