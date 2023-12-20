import * as React from 'react';
import {useCallback, useEffect, useReducer, useState} from 'react';
import {exitLobby, waittingInLobby} from '../../services/gamesService';
import {ProblemModel} from '../../services/media/ProblemModel';
import {FindGameOutput} from '../../services/models/games/FindGameOutputModel';
import {Link, Navigate, useParams} from 'react-router-dom';
import {webRoutes} from '../../App';
import {replacePathVariables} from '../utils/replacePathVariables';
import {Button} from '@mui/material';

type State =
    | { tag: 'loading' }
    | { tag: 'in-lobby'; lobbyId: number }
    | { tag: 'error'; message: string }
    | { tag: 'redirectToGame'; gameId: number }
    | { tag: 'redirectHome' };

type Action =
    | { type: 'start-loading' }
    | { type: 'error'; message: string }
    | { type: 'start-game'; gameId: number }
    | { type: 'waiting-in-lobby'; lobbyId: number }
    | { type: 'leave-lobby' };

function reducer(state: State, action: Action): State {
    switch (action.type) {
        case 'start-loading':
            return { tag: 'loading' };
        case 'start-game':
            return { tag: 'redirectToGame', gameId: action.gameId };
        case 'error':
            return { tag: 'error', message: action.message };
        case 'waiting-in-lobby':
            return { tag: 'in-lobby', lobbyId: action.lobbyId };
        case 'leave-lobby':
            return { tag: 'redirectHome' };
        default:
            return state;
    }
}

function fetchLobby(
    lobbyId: number,
    isFetching: boolean,
    setIsFetching: (isFetching: boolean) => void,
    dispatch: (action: Action) => void
) {
    if (isFetching) return;
    setIsFetching(true);
    waittingInLobby(lobbyId)
        .then(result => {
            const errorData = result.json as ProblemModel;
            const successData = result.json as unknown as FindGameOutput;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                if (successData.class.find(c => c == 'lobby') != undefined) {
                    dispatch({ type: 'waiting-in-lobby', lobbyId: successData.properties.id });
                } else if (successData.class.find(c => c == 'game') != undefined) {
                    const gameId = successData.properties.id;
                    dispatch({ type: 'start-game', gameId: gameId });
                }
            }
            setIsFetching(false);
        })
        .catch(error => {
            dispatch({ type: 'error', message: error });
            setIsFetching(false);
        });
}

export function Lobby() {
    const { lobbyId } = useParams();
    const lobbyIdNumber = parseInt(lobbyId);
    const [state, dispatch] = useReducer(reducer, { tag: 'loading' });
    const [isFetching, setIsFetching] = useState(false);
    const [isPollingActive, setIsPollingActive] = useState(false);

    useEffect(() => {
        if (state.tag === 'loading') {
            fetchLobby(lobbyIdNumber, isFetching, setIsFetching, dispatch);
        }
    }, [state, lobbyIdNumber, isFetching, setIsFetching, dispatch]);

    const startPollingLobbyStatus = useCallback(
        lobbyId => {
            setIsPollingActive(true);
            return setInterval(() => {
                if (!isPollingActive) return;
                waittingInLobby(lobbyId).then(result => {
                    const errorData = result.json as ProblemModel;
                    const successData = result.json as unknown as FindGameOutput;
                    if (result.contentType === 'application/problem+json') {
                        dispatch({type: 'error', message: errorData.detail});
                        setIsPollingActive(false);
                    } else if (result.contentType === 'application/vnd.siren+json') {
                        if (successData.class.find(c => c == 'lobby') != undefined) {
                            dispatch({type: 'waiting-in-lobby', lobbyId: successData.properties.id});
                        } else if (successData.class.find(c => c == 'game') != undefined) {
                            const gameId = successData.properties.id;
                            setIsPollingActive(false);
                            dispatch({type: 'start-game', gameId: gameId});
                        }
                    }
                });
            }, 3000);
        },
        [setIsPollingActive, isPollingActive]
    );

    useEffect(() => {
        let intervalId;
        if (state.tag === 'in-lobby') {
            intervalId = startPollingLobbyStatus(state.lobbyId);
            return () => {
                if (intervalId) {
                    clearInterval(intervalId);
                }
                setIsPollingActive(false);
            };
        }
    }, [state, startPollingLobbyStatus]);

    function handleLeaveLobby(e: React.MouseEvent<HTMLButtonElement, MouseEvent>, lobbyId: number) {
        e.preventDefault();
        setIsPollingActive(false);
        dispatch({ type: 'leave-lobby' });
        exitLobby(lobbyId)
            .then(result => {
                if (result.contentType === 'application/problem+json') {
                    const errorData = result.json as ProblemModel;
                    dispatch({ type: 'error', message: errorData.detail });
                } else {
                    dispatch({ type: 'leave-lobby' });
                }
            })
            .catch(error => {
                dispatch({ type: 'error', message: error });
            });
    }

    switch (state.tag) {
        case 'loading':
            return <div className="container">Loading ...</div>;

        case 'redirectToGame':
            return <Navigate to={replacePathVariables(webRoutes.game, [state.gameId])} />;

        case 'in-lobby':
            return (
                <div className="container">
                    Waiting in lobby {state.lobbyId}
                    <p>
                        <Button variant="contained" onClick={e => handleLeaveLobby(e, state.lobbyId)}>
                            Leave lobby
                        </Button>
                    </p>
                </div>
            );
        case 'error':
            return (
                <div className="container">
                    You dont belong to this lobby
                    <p>
                        <Link to={webRoutes.home}>Go back Home</Link>
                    </p>
                </div>
            );
        case 'redirectHome':
            return <Navigate to={webRoutes.home} />;

        default:
            return (
                <div className="container">
                    <h1>Unknown state</h1>
                </div>
            );
    }
}
