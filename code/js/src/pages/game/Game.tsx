import * as React from 'react';
import { exitGame, getGame } from '../../services/gameServices';
import { makeMove } from '../../services/gameServices';
import { ProblemModel } from '../../services/media/ProblemModel';
import { GameOutput } from '../../services/models/games/GameOutputModel';
import { renderBoard } from './BoardDraw';
import { useCurrentUserId, useCurrentUserName } from '../GomokuContainer';
import { useParams, Link } from 'react-router-dom';
import { Entity } from '../../services/media/siren/Entity';

function columnIndexToLetter(index: number) {
    return String.fromCharCode(97 + index);
}

type State =
    | { tag: 'loading' }
    | { tag: 'notYourTurn'; boardSize: number; grid: string[]; opponent: string }
    | { tag: 'your-turn'; boardSize: number; grid: string[]; opponent: string; message: string | undefined }
    | { tag: 'win'; boardSize: number; grid: string[]; opponent: string }
    | { tag: 'draw'; boardSize: number; grid: string[]; opponent: string }
    | { tag: 'lost'; boardSize: number; grid: string[]; opponent: string }
    | { tag: 'error'; message: string };

type Action =
    | { type: 'start-fetching' }
    | { type: 'set-turn'; isYourTurn: boolean; BOARD_SIZE: number; grid: string[]; opponent: string; message?: string }
    | { type: 'set-not-your-turn'; BOARD_SIZE: number; grid: string[]; opponent: string }
    | { type: 'win'; BOARD_SIZE: number; grid: string[]; opponent: string }
    | { type: 'draw'; BOARD_SIZE: number; grid: string[]; opponent: string }
    | { type: 'lose'; BOARD_SIZE: number; grid: string[]; opponent: string }
    | { type: 'error'; message: string };

function gameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'start-fetching':
            return { tag: 'loading' };
        case 'set-not-your-turn':
            return {
                ...state,
                tag: 'notYourTurn',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
            };
        case 'set-turn':
            return {
                tag: 'your-turn',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
            };
        case 'win':
            return {
                ...state,
                tag: 'win',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
            };
        case 'lose':
            return {
                ...state,
                tag: 'lost',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
            };
        case 'draw':
            return {
                ...state,
                tag: 'draw',
                boardSize: action.BOARD_SIZE,
                grid: action.grid,
                opponent: action.opponent,
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
    users: Entity<unknown>[],
    opponent?: string
) {
    const opponentUsername: string = 'sasas';
    // if (opponent == undefined) {
    //     if (game.properties.hostId === userId) {
    //         const opponent = users.find(e => e.properties.id !== game.properties.hostId);
    //         opponentUsername = opponent ? opponent.properties.username : undefined;
    //     } else {
    //         const opponent = users.find(e => e.properties.id !== game.properties.guestId);
    //         opponentUsername = opponent ? opponent.properties.username : undefined;
    //     }
    // }
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
                });
            } else {
                dispatch({
                    type: 'lose',
                    BOARD_SIZE: game.properties.variant.boardSize,
                    grid: game.properties.board.grid,
                    opponent: opp,
                });
            }
        } else {
            console.log('draw');
            dispatch({
                type: 'draw',
                BOARD_SIZE: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
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

function fetchGame(currentGameId, opponent, isFetching, userId, dispatch, setIsFetching) {
    if (isFetching) return;
    setIsFetching(true);
    getGame(currentGameId).then(result => {
        const errorData = result.json as ProblemModel;
        const successData = result.json as unknown as GameOutput;
        if (result.contentType === 'application/problem+json') {
            dispatch({ type: 'error', message: errorData.detail });
            setIsFetching(false);
        } else if (successData.class.find(c => c == 'game') != undefined) {
            const users = successData.entities;
            game(successData, dispatch, userId, users, opponent);
            setIsFetching(false);
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

    function handleMakeMove(rowIndex: number, colIndex: number, size: number, grid: string[], opponent: string) {
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
                    dispatch({ type: 'win', BOARD_SIZE: size, grid: grid, opponent: opponent });
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
        if (state.tag == 'loading' && !isFetching) {
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
                <>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div>Turn:Not your turn </div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to="/games" onClick={() => handleLeaveGame(currentGameId, dispatch, setIsFetching)}>
                            Leave Game
                        </Link>
                    </div>
                    <div></div>
                </>
            );
        case 'your-turn':
            return (
                <>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent, handleMakeMove)}</div>
                    <div>Turn: Your turn </div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>{state.message}</div>
                    <div>
                        <Link to="/games" onClick={() => handleLeaveGame(currentGameId, dispatch, setIsFetching)}>
                            Leave Game
                        </Link>
                    </div>
                </>
            );
        case 'lost':
            return (
                <>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div> You Lost the game... Player: {username}</div>
                    <div> Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to={'/games'}>Start New Game</Link>
                    </div>
                </>
            );
        case 'win':
            return (
                <>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div>You won the game! Player: {username}</div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to={'/games'}>Start New Game</Link>
                    </div>
                </>
            );
        case 'draw':
            return (
                <>
                    <div>{renderBoard(state.boardSize, state.grid, state.opponent)}</div>
                    <div>Draw</div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>
                        <Link to={'/games'}>Start New Game</Link>
                    </div>
                </>
            );
    }
}
