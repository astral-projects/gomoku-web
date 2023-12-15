import * as React from 'react';
import { exitGame, getGame } from '../../services/gameServices';
import { makeMove } from '../../services/gameServices';
import { ProblemModel } from '../../services/media/ProblemModel';
import { GameOutput } from '../../services/models/games/GameOutputModel';
import { renderBoard } from './BoardDraw';
import { useCurrentUserId, useCurrentUserName } from '../GomokuContainer';
import { Entity } from '../../services/media/siren/Entity';
import { useNavigate, useParams } from 'react-router-dom';
import { GameEntities } from '../../services/models/games/GameEntitiesModel';

function columnIndexToLetter(index: number) {
    return String.fromCharCode(97 + index);
}

type FindGameState =
    | { tag: 'loading' }
    | { tag: 'notYourTurn'; boardSize: number; grid: string[] }
    | { tag: 'play'; boardSize: number; grid: string[] }
    | { tag: 'leave' }
    | { tag: 'win'; boardSize: number; grid: string[] }
    | { tag: 'lost'; boardSize: number; grid: string[] }
    | { tag: 'error'; message: string };

type FindGameAction =
    | { type: 'start-fetching' }
    | { type: 'set-turn'; isYourTurn: boolean; BOARD_SIZE: number; grid: string[] }
    | { type: 'set-not-your-turn'; BOARD_SIZE: number; grid: string[] }
    | { type: 'leave-game' }
    | { type: 'win'; BOARD_SIZE: number; grid: string[] }
    | { type: 'lose'; BOARD_SIZE: number; grid: string[] }
    | { type: 'error'; message: string };

function gameReducer(state: FindGameState, action: FindGameAction): FindGameState {
    switch (action.type) {
        case 'start-fetching':
            return { tag: 'loading' };
        case 'set-not-your-turn':
            return { ...state, tag: 'notYourTurn', boardSize: action.BOARD_SIZE, grid: action.grid };
        case 'set-turn':
            return { tag: 'play', boardSize: action.BOARD_SIZE, grid: action.grid };
        case 'leave-game':
            return { tag: 'leave' };
        case 'win':
            return { ...state, tag: 'win', boardSize: action.BOARD_SIZE, grid: action.grid };
        case 'lose':
            return { ...state, tag: 'lost', boardSize: action.BOARD_SIZE, grid: action.grid };
        case 'error':
            return { ...state, tag: 'error', message: action.message };
        default:
            return state;
    }
}

export function Game() {
    const [state, dispatch] = React.useReducer(gameReducer, { tag: 'loading' });
    const userId = useCurrentUserId();
    const userName = useCurrentUserName();
    const { gameId } = useParams();
    const currentGameId = parseInt(gameId);
    const [isMoveInProgress, setIsMoveInProgress] = React.useState(false);
    const navigate = useNavigate();

    const [isFetching, setIsFetching] = React.useState(false);

    const fetchGame = React.useCallback(
        currentGameId => {
            if (isFetching) return;
            setIsFetching(true);
            getGame(currentGameId).then(result => {
                const errorData = result.json as ProblemModel;
                const successData = result.json as unknown as GameOutput;
                if (result.contentType === 'application/problem+json') {
                    dispatch({ type: 'error', message: errorData.detail });
                    setIsFetching(false);
                } else if (result.contentType === 'application/vnd.siren+json') {
                    if (successData.class.find(c => c == 'game') != undefined) {
                        if (successData.properties.state.name == 'finished') {
                            if (successData.properties.board.winner != undefined) {
                                const isWin =
                                    successData.properties.board.winner == 'W'
                                        ? successData.properties.hostId == userId
                                        : successData.properties.guestId == userId;
                                if (isWin) {
                                    dispatch({
                                        type: 'win',
                                        BOARD_SIZE: successData.properties.variant.boardSize,
                                        grid: successData.properties.board.grid,
                                    });
                                } else {
                                    dispatch({
                                        type: 'lose',
                                        BOARD_SIZE: successData.properties.variant.boardSize,
                                        grid: successData.properties.board.grid,
                                    });
                                }
                            } else {
                                dispatch({ type: 'error', message: 'Something went wrong.' });
                            }
                        } else {
                            const isYourTurn =
                                successData.properties.board.turn.player == 'W'
                                    ? successData.properties.hostId == userId
                                    : successData.properties.guestId == userId;
                            if (isYourTurn) {
                                dispatch({
                                    type: 'set-turn',
                                    isYourTurn: isYourTurn,
                                    BOARD_SIZE: successData.properties.variant.boardSize,
                                    grid: successData.properties.board.grid,
                                });
                            } else {
                                dispatch({
                                    type: 'set-not-your-turn',
                                    BOARD_SIZE: successData.properties.variant.boardSize,
                                    grid: successData.properties.board.grid,
                                });
                            }
                        }
                        setIsFetching(false);
                    }
                }
            });
        },
        [isFetching, userId]
    );

    React.useEffect(() => {
        if (state.tag == 'loading' && !isFetching) {
            fetchGame(currentGameId);
        }
        if (state.tag === 'notYourTurn') {
            const interval = setInterval(() => {
                fetchGame(currentGameId);
            }, 2000);

            return () => clearInterval(interval);
        }
    }, [setIsFetching, isFetching, state.tag, currentGameId, fetchGame, userId]);

    const handleIntersectionClick = (rowIndex, colIndex, size, grid) => {
        if (isMoveInProgress || rowIndex === 0 || colIndex === 0 || rowIndex === size || colIndex === size) {
            return;
        }
        setIsMoveInProgress(true);
        const colLetter = columnIndexToLetter(colIndex - 1);
        makeMove(currentGameId, { col: colLetter, row: rowIndex }).then(result => {
            const errorData = result.json as ProblemModel;
            if (result.contentType === 'application/problem+json') {
                if (errorData.detail.includes('The game with id') && errorData.detail.includes('is already finished')) {
                    dispatch({ type: 'win', BOARD_SIZE: size, grid: grid });
                } else {
                    dispatch({ type: 'error', message: errorData.detail });
                }
                setIsMoveInProgress(false);
            } else if (result.contentType === 'application/vnd.siren+json') {
                const successData = result.json as unknown as GameOutput;
                const entities = successData.entities as Entity<GameEntities>[];
                if (successData.class.find(c => c == 'game') != undefined) {
                    if (entities[0].properties.state.name == 'finished') {
                        if (entities[0].properties.board.winner != undefined) {
                            const isWin =
                                entities[0].properties.board.winner == 'W'
                                    ? entities[0].properties.hostId == userId
                                    : entities[0].properties.guestId == userId;
                            if (isWin) {
                                dispatch({
                                    type: 'win',
                                    BOARD_SIZE: entities[0].properties.variant.boardSize,
                                    grid: entities[0].properties.board.grid,
                                });
                            } else {
                                dispatch({
                                    type: 'lose',
                                    BOARD_SIZE: entities[0].properties.variant.boardSize,
                                    grid: entities[0].properties.board.grid,
                                });
                            }
                        }
                    } else {
                        const isYourTurn =
                            entities[0].properties.board.turn.player == 'W'
                                ? entities[0].properties.hostId == userId
                                : entities[0].properties.guestId == userId;
                        if (isYourTurn) {
                            dispatch({
                                type: 'set-turn',
                                isYourTurn: isYourTurn,
                                BOARD_SIZE: entities[0].properties.variant.boardSize,
                                grid: entities[0].properties.board.grid,
                            });
                        } else {
                            dispatch({
                                type: 'set-not-your-turn',
                                BOARD_SIZE: entities[0].properties.variant.boardSize,
                                grid: entities[0].properties.board.grid,
                            });
                        }
                    }
                    setIsMoveInProgress(false);
                }
            }
        });
    };

    const handleLeaveGame = gameId => {
        setIsFetching(false);
        exitGame(gameId).then(result => {
            const errorData = result.json as ProblemModel;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            } else if (result.contentType === 'application/vnd.siren+json') {
                dispatch({ type: 'leave-game' });
                navigate('/games');
            }
        });
    };

    switch (state.tag) {
        case 'loading':
            return <div>Loading game...</div>;
        case 'notYourTurn':
            return (
                <div>
                    {renderBoard(state.boardSize, state.grid, null)}
                    Turn: Not your turn Player: {userName}
                    <button onClick={() => handleLeaveGame(currentGameId)}>Leave Game</button>
                </div>
            );
        case 'play':
            return (
                <>
                    {renderBoard(state.boardSize, state.grid, handleIntersectionClick)}
                    Turn: Your turn Player: {userName}
                    <button onClick={() => handleLeaveGame(currentGameId)}>Leave Game</button>
                </>
            );
        case 'leave':
            return <div>You have left the game.</div>;

        case 'lost':
            return (
                <>
                    {renderBoard(state.boardSize, state.grid)}
                    You Lost the game... Player: {userName}
                    <button onClick={() => navigate('/games')}>Start New Game</button>
                </>
            );
        case 'win':
            return (
                <>
                    {renderBoard(state.boardSize, state.grid)}
                    You won the game! Player: {userName}
                    <button onClick={() => navigate('/games')}>Start New Game</button>
                </>
            );
    }
}
