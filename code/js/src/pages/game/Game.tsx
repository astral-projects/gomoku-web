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
import { useEffect } from 'react';

function columnIndexToLetter(index: number) {
    return String.fromCharCode(97 + index);
}

/**
 * The state of the game page.
 * @param tag - The current state of the game page.
 * @param boardSize - The size of the board.
 * @param grid - The grid of the board.
 * @param opponent - The opponent of the game.
 * @param message - The message to be displayed.
 * @param IsYourTurn - The boolean that indicates if it is the turn of the user.
 * @param errorMessage - The error message to be displayed.
 */
type State =
    | { tag: 'loading' }
    | {
          tag: 'play';
          boardSize: number;
          grid: string[];
          opponent: string;
          message: string;
          IsYourTurn: boolean;
          errorMessage?: string;
      }
    | { tag: 'game-state'; boardSize: number; grid: string[]; opponent: string; message: string }
    | { tag: 'error'; message: string };
/**
 * The action of the game page.
 * @param type - The type of the action.
 * @param boardSize - The size of the board.
 * @param grid - The grid of the board.
 * @param opponent - The opponent of the game.
 * @param message - The message to be displayed.
 * @param errorMessage - The error message to be displayed.
 */
export type Action =
    | { type: 'start-fetching' }
    | {
          type: 'set-turn';
          boardSize: number;
          grid: string[];
          opponent: string;
          IsYourTurn: boolean;
          message: string;
          errorMessage?: string;
      }
    | {
          type: 'set-not-your-turn';
          boardSize: number;
          grid: string[];
          opponent: string;
          IsYourTurn: boolean;
          message: string;
          errorMessage?: string;
      }
    | { type: 'win'; boardSize: number; grid: string[]; opponent: string; message: string }
    | { type: 'draw'; boardSize: number; grid: string[]; opponent: string; message: string }
    | { type: 'lost'; boardSize: number; grid: string[]; opponent: string; message: string }
    | { type: 'error'; message: string };

/**
 * The reducer of the game page.
 * @param state - The current state of the game page.
 * @param action - The action to be performed.
 * @returns The new state of the game page.
 */
function gameReducer(state: State, action: Action): State {
    switch (action.type) {
        case 'start-fetching':
            return { tag: 'loading' };
        case 'set-turn':
        case 'set-not-your-turn':
            return {
                ...state,
                tag: 'play',
                boardSize: action.boardSize,
                grid: action.grid,
                opponent: action.opponent,
                message: action.message,
                IsYourTurn: action.IsYourTurn,
                errorMessage: action.errorMessage,
            };
        case 'lost':
        case 'win':
        case 'draw':
            return {
                ...state,
                tag: 'game-state',
                boardSize: action.boardSize,
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

/**
 * Determines the state of the game in argeement with the game output.
 * @param game - The game output.
 * @param dispatch - The dispatch function.
 * @param userId - The id of the user.
 * @param opponent - The opponent of the game can be optional because if its the first fetch we dont have opponet so optional its for that case.
 * @param users - The users of the game can be too optional because the method makeMove doesn´t have the users information that´s why is optional.
 */
function game(
    game: GameOutput,
    dispatch: React.Dispatch<Action>,
    userId: number,
    opponent?: string,
    users?: Entity<UserEntity>[]
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
    if (game.properties.state.name == 'finished') {
        if (game.properties.board.winner != undefined) {
            const isWin =
                game.properties.board.winner == 'W'
                    ? game.properties.hostId == userId
                    : game.properties.guestId == userId;
            if (isWin) {
                dispatch({
                    type: 'win',
                    boardSize: game.properties.variant.boardSize,
                    grid: game.properties.board.grid,
                    opponent: opp,
                    message: 'You won the game!',
                });
            } else {
                dispatch({
                    type: 'lost',
                    boardSize: game.properties.variant.boardSize,
                    grid: game.properties.board.grid,
                    opponent: opp,
                    message: 'You lost the game!',
                });
            }
        } else {
            dispatch({
                type: 'draw',
                boardSize: game.properties.variant.boardSize,
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
                boardSize: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
                IsYourTurn: isYourTurn,
                message: 'Turn: Your turn',
            });
        } else {
            dispatch({
                type: 'set-not-your-turn',
                boardSize: game.properties.variant.boardSize,
                grid: game.properties.board.grid,
                opponent: opp,
                IsYourTurn: isYourTurn,
                message: 'Turn: Not your turn',
            });
        }
    }
}

/**
 * Fetches the game.
 * @param currentGameId - The id of the current game.
 * @param userId - The id of the user.
 *  @param isFetching - The boolean that indicates if the game is fetching.
 * @param setIsFetching - The function that sets the boolean that indicates if the game is fetching.
 * @param dispatch - The dispatch function.
 * @param opponent - The opponent of the game can be optional because if its the first fetch we dont have opponet so optional its for that case.
 */
function fetchGame(
    currentGameId: number,
    userId: number,
    isFetching: boolean,
    setIsFetching: (isFetching: boolean) => void,
    dispatch: (action: Action) => void,
    opponent?: string
) {
    if (isFetching) return;
    setIsFetching(true);
    getGame(currentGameId).then(result => {
        const errorData = result.json as ProblemModel;
        const successData = result.json as unknown as GameOutput;
        if (result.contentType === 'application/problem+json') {
            dispatch({ type: 'error', message: errorData.detail });
        } else if (successData.class.find(c => c == 'game') != undefined) {
            const users = successData.entities as Entity<UserEntity>[];
            game(successData, dispatch, userId, opponent, users);
        }
        setIsFetching(false);
    });
}

/**
 * Handles the move of the game doing the request to the server. and dispatching the action.
 * @param rowIndex
 * @param colIndex
 * @param size
 * @param grid
 * @param opponent
 * @param currentGameId
 * @param userId
 * @param dispatch
 * @param isMoveInProgress
 * @param setIsMoveInProgress
 * @returns
 */
function handleMakeMove(
    rowIndex: number,
    colIndex: number,
    IsYourTurn: boolean,
    size: number,
    grid: string[],
    opponent: string,
    currentGameId: number,
    userId: number,
    dispatch: (action: Action) => void,
    isMoveInProgress: boolean,
    setIsMoveInProgress: (isMoveInProgress: boolean) => void
) {
    if (!IsYourTurn || isMoveInProgress || rowIndex === 0 || colIndex === 0 || rowIndex === size || colIndex === size) {
        return;
    }
    setIsMoveInProgress(true);
    const colLetter = columnIndexToLetter(colIndex - 1);
    makeMove(currentGameId, { col: colLetter, row: rowIndex }).then(result => {
        const errorData = result.json as ProblemModel;
        const successData = result.json as unknown as GameOutput;
        if (result.contentType === 'application/problem+json') {
            if (errorData.title == 'Game already finished') {
                dispatch({
                    type: 'win',
                    boardSize: size,
                    grid: grid,
                    opponent: opponent,
                    message: 'You won the game!',
                });
            } else if (errorData.title == 'Position taken') {
                dispatch({
                    type: 'set-turn',
                    boardSize: size,
                    grid: grid,
                    opponent: opponent,
                    IsYourTurn: true,
                    message: 'Turn: Your turn',
                    errorMessage: errorData.detail,
                });
            } else {
                dispatch({ type: 'error', message: errorData.detail });
            }
        } else if (successData.class.find(c => c == 'game') != undefined) {
            game(successData, dispatch, userId, opponent);
        }
        setIsMoveInProgress(false);
    });
}

/**
 * Handles the leave game doing the request to the server and dispatching the action.
 * @param gameId
 * @param dispatch
 * @param setIsFetching
 */
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

/**
 * @returns The game page.
 */
export function Game() {
    const [state, dispatch] = React.useReducer(gameReducer, { tag: 'loading' });
    const userId = useCurrentUserId();
    const username = useCurrentUserName();
    const { gameId } = useParams();
    const currentGameId = parseInt(gameId);
    const [isMoveInProgress, setIsMoveInProgress] = React.useState(false);
    const [isFetching, setIsFetching] = React.useState(false);

    /**
     * The useEffect is invoked when the game state is 'loading', is not currently fetching (isFetching is false), and userId is not undefined.
     * When these conditions are true, the fetchGame function is called with the specified arguments.
     * if the state tag is 'notYourTurn,' an interval is set to call the fetchGame function every 5000 milliseconds.
     * When the component is unmounted or before the next rendering, the interval is cleared.
     * This is done to prevent unnecessary calls to the fetchGame function.
     * The list of dependencies for the useEffect is defined as [setIsFetching, isFetching, state, currentGameId, userId].
     * This means that the useEffect will be triggered again whenever any of these dependencies change.
     */
    useEffect(() => {
        if (state.tag == 'loading' && !isFetching && userId != undefined) {
            fetchGame(currentGameId, userId, isFetching, setIsFetching, dispatch);
        }
        if (state.tag === 'play' && !state.IsYourTurn) {
            const interval = setInterval(() => {
                fetchGame(currentGameId, userId, isFetching, setIsFetching, dispatch, state.opponent);
            }, 5000);
            return () => clearInterval(interval);
        }
    }, [setIsFetching, isFetching, state, currentGameId, userId]);

    switch (state.tag) {
        case 'loading':
            return <div>Loading game...</div>;

        case 'error':
            return (
                <div>
                    <div>{state.message}</div>
                    <div>
                        <Link to={webRoutes.games}>Start New Game</Link>
                    </div>
                </div>
            );
        case 'play':
            return (
                <div>
                    <div>
                        {renderBoard(
                            state.boardSize,
                            state.grid,
                            state.opponent,
                            state.IsYourTurn,
                            currentGameId,
                            userId,
                            dispatch,
                            isMoveInProgress,
                            setIsMoveInProgress,
                            handleMakeMove
                        )}
                    </div>
                    <div>{state.message} </div>
                    <div>Player: {username}</div>
                    <div> Opponent:{state.opponent}</div>
                    <div>{state.errorMessage}</div>
                    <div>
                        <Link
                            to={webRoutes.games}
                            onClick={() => handleLeaveGame(currentGameId, dispatch, setIsFetching)}
                        >
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
