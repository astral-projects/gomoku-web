import * as React from 'react';
import { exitGame, getGame } from '../../services/gameServices';
import { makeMove } from '../../services/gameServices';
import { ProblemModel } from '../../services/media/ProblemModel';
import { GameOutput } from '../../services/models/games/GameOutputModel';
import { renderBoard } from './BoardDraw';
import { useCurrentUserId, useCurrentUserName } from '../GomokuContainer';
import { useParams, Link } from 'react-router-dom';
import { Entity } from '../../services/media/siren/Entity';
import { UserEntity } from '../../services/models/users/UserEntityOutputModel';
import { webRoutes } from '../../App';

function columnIndexToLetter(index: number) {
    return String.fromCharCode(97 + index);
}

type State =
    | { tag: 'loading' }
    | { tag: 'notYourTurn'; boardSize: number; grid: string[]; opponent: string }
    | { tag: 'your-turn'; boardSize: number; grid: string[]; opponent: string; message: string | undefined }
    | { tag: 'game-state'; boardSize: number; grid: string[]; opponent: string; message: string }
    | { tag: 'error'; message: string };

export type Action =
    | { type: 'start-fetching' }
    | { type: 'set-turn'; isYourTurn: boolean; BOARD_SIZE: number; grid: string[]; opponent: string; message?: string }
    | { type: 'set-not-your-turn'; BOARD_SIZE: number; grid: string[]; opponent: string }
    | { type: 'win'; BOARD_SIZE: number; grid: string[]; opponent: string; message: string }
    | { type: 'draw'; BOARD_SIZE: number; grid: string[]; opponent: string; message: string }
    | { type: 'lose'; BOARD_SIZE: number; grid: string[]; opponent: string; message: string }
    | { type: 'error'; message: string };

function gameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'start-fetching':
            return { tag: 'loading' };
        case 'set-turn':
            return {
                tag: 'your-turn',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
            };
        case 'set-not-your-turn':
            return {
                ...state,
                tag: 'notYourTurn',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
            };
        case 'lose':
            return {
                ...state,
                tag: 'game-state',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
            };
        case 'win':
            return {
                ...state,
                tag: 'game-state',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
            };
        case 'draw':
            return {
                ...state,
                tag: 'game-state',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
            };
        case 'error':
            return { ...state, tag: 'error', message: action.message };
        default:
            return state;
    }
}

function game(
    game: GameOutput,
    dispatch: React.Dispatch<Action>,
    userId: number,
    users: Entity<UserEntity>[],
    opponent?: string
) {
    let opponentUsername: string;
    if (opponent == undefined) {
        if (game.properties.hostId === userId) {
            const opponent = users.find(e => e.properties.id !== game.properties.hostId);
            opponentUsername = opponent ? opponent.properties.username : undefined;
        } else {
            const opponent = users.find(e => e.properties.id !== game.properties.guestId);
            opponentUsername = opponent ? opponent.properties.username : undefined;
        }
    }
    const opp = opponent !== undefined ? opponent : opponentUsername;
    console.log('opp' + opp);
    console.log('dentro do gamew');
    if (game.properties.state.name == 'finished') {
        if (game.properties.board.winner != undefined) {
            const isWin =
                game.properties.board.winner == 'W'
                    ? game.properties.hostId == userId
                    : game.properties.guestId == userId;
            if (isWin) {
                dispatch({
                    type: 'win',
                    BOARD_SIZE: game.properties.variant.boardSize,
                    grid: game.properties.board.grid,
                    opponent: opp,
                    message: 'You won the game!',
                });
            } else {
                dispatch({
                    type: 'lose',
                    BOARD_SIZE: game.properties.variant.boardSize,
                    grid: game.properties.board.grid,
                    opponent: opp,
                    message: 'You lost the game!',
                });
            }
        } else {
            console.log('draw');
            dispatch({
                type: 'draw',
                BOARD_SIZE: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
                message: 'Draw!',
            });
        }
    } else {
        const isYourTurn =
            game.properties.board.turn.player == 'W'
                ? game.properties.hostId == userId
                : game.properties.guestId == userId;
        if (isYourTurn) {
            dispatch({
                type: 'set-turn',
                isYourTurn: isYourTurn,
                BOARD_SIZE: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
            });
        } else {
            dispatch({
                type: 'set-not-your-turn',
                BOARD_SIZE: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
            });
        }
    }
}

function fetchGame(
    currentGameId: number,
    opponent: string,
    isFetching: boolean,
    userId: number,
    dispatch: (action: Action) => void,
    setIsFetching: (isFetching: boolean) => void
) {
    if (isFetching) return;
    setIsFetching(true);
    getGame(currentGameId).then(result => {
        const errorData = result.json as ProblemModel;
        const successData = result.json as unknown as GameOutput;
        if (result.contentType === 'application/problem+json') {
            dispatch({ type: 'error', message: errorData.detail });
            setIsFetching(false);
        } else if (successData.class.find(c => c == 'game') != undefined) {
            const users = successData.entities as Entity<UserEntity>[];
            game(successData, dispatch, userId, users, opponent);
            setIsFetching(false);
        }
    });
}

function handleMakeMove(
    rowIndex: number,
    colIndex: number,
    size: number,
    grid: string[],
    opponent: string,
    currentGameId: number,
    userId: number,
    dispatch: (action: Action) => void,
    isMoveInProgress: boolean,
    setIsMoveInProgress: (isMoveInProgress: boolean) => void
) {
    if (isMoveInProgress || rowIndex === 0 || colIndex === 0 || rowIndex === size || colIndex === size) {
        return;
    }
    setIsMoveInProgress(true);
    const colLetter = columnIndexToLetter(colIndex - 1);
    makeMove(currentGameId, { col: colLetter, row: rowIndex }).then(result => {
        const errorData = result.json as ProblemModel;
        const successData = result.json as unknown as GameOutput;
        if (result.contentType === 'application/problem+json') {
            if (errorData.detail.includes('The game with id') && errorData.detail.includes('is already finished')) {
                dispatch({
                    type: 'win',
                    BOARD_SIZE: size,
                    grid: grid,
                    opponent: opponent,
                    message: 'You won the game!',
                });
            } else if (errorData.title == 'Position taken') {
                dispatch({
                    type: 'set-turn',
                    isYourTurn: true,
                    BOARD_SIZE: size,
                    grid: grid,
                    opponent: opponent,
                    message: errorData.detail,
                });
            } else {
                dispatch({ type: 'error', message: errorData.detail });
            }
            setIsMoveInProgress(false);
        } else if (successData.class.find(c => c == 'game') != undefined) {
            game(successData, dispatch, userId, undefined, opponent);
            setIsMoveInProgress(false);
        }
    });
}

export function Game() {
    const [state, dispatch] = React.useReducer(gameReducer, { tag: 'loading' });
    const userId = useCurrentUserId();
    const username = useCurrentUserName();
    const { gameId } = useParams();
    const currentGameId = parseInt(gameId);
    const [isMoveInProgress, setIsMoveInProgress] = React.useState(false);
    const [isFetching, setIsFetching] = React.useState(false);

    function handleLeaveGame(
        gameId: number,
        dispatch: (action: Action) => void,
        setIsFetching: (isFetching: boolean) => void
    ) {
        setIsFetching(false);
        exitGame(gameId).then(result => {
            const errorData = result.json as ProblemModel;
            if (result.contentType === 'application/problem+json') {
                dispatch({ type: 'error', message: errorData.detail });
            }
        });
    }

    React.useEffect(() => {
        if (state.tag == 'loading' && !isFetching && userId != undefined) {
            fetchGame(currentGameId, undefined, isFetching, userId, dispatch, setIsFetching);
        }
        if (state.tag === 'notYourTurn') {
            const interval = setInterval(() => {
                fetchGame(currentGameId, state.opponent, isFetching, userId, dispatch, setIsFetching);
            }, 2000);

            return () => clearInterval(interval);
        }
    }, [setIsFetching, isFetching, state, currentGameId, userId]);

    switch (state.tag) {
        case 'loading':
            return <div>Loading game...</div>;
        case 'notYourTurn':
            return (
                <div>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div>Turn:Not your turn </div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to={webRoutes.games} onClick={() => handleLeaveGame(currentGameId, dispatch, setIsFetching)}>
                            Leave Game
                        </Link>
                    </div>
                </div>
            );
        case 'your-turn':
            return (
                <div>
                    <div>
                        {renderBoard(
                            state.boardSize,
                            state.grid,
                            state.opponent,
                            currentGameId,
                            userId,
                            dispatch,
                            isMoveInProgress,
                            setIsMoveInProgress,
                            handleMakeMove
                        )}
                    </div>
                    <div>Turn: Your turn </div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>{state.message}</div>
                    <div>
                        <Link to={webRoutes.games} onClick={() => handleLeaveGame(currentGameId, dispatch, setIsFetching)}>
                            Leave Game
                        </Link>
                    </div>
                </div>
            );
        case 'game-state':
            return (
                <div>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div> {state.message}</div>
                    <div> Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to={webRoutes.games}>Start New Game</Link>
                    </div>
                </div>
            );
    }
}
