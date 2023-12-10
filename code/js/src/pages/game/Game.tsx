import * as React from "react";
import { exitGame, getGame } from "../../services/gameServices";
import { makeMove } from "../../services/gameServices";
import { ProblemModel } from '../../services/media/ProblemModel';
import { GameOutput } from "../../services/users/models/games/GameOutputModel";
import { useCurrentGameId } from "../gomokuContainer/GomokuContainer";
import { useCurrentUser } from "../gomokuContainer/GomokuContainer";
import { Entity } from "../../services/media/siren/Entity";
import { useNavigate } from "react-router-dom";




const cellSize = 30;

function columnIndexToLetter(index) {
    return String.fromCharCode(97 + index);
}

type GameProperties = {
    id: number;
    state: { name: string };
    variant: {
        id: number;
        name: string;
        openingRule: string;
        boardSize: number;
    };
    board: {
        grid: string[];
        turn: {
            player: string;
            timeLeftInSec: {
                value: number;
            };
        };
    };
    createdAt: string;
    updatedAt: string;
    hostId: number;
    guestId: number;
};

type FindGameState =
  | { tag: 'loading' }
  | { tag: 'notYourTurn', boardSize: number, grid: string[] }
  | { tag: 'play', boardSize: number, grid: string[] }
  | { tag: 'leave' }
  | { tag: 'win', boardSize: number, grid: string[] }
  | { tag: 'error', message: string };


type FindGameAction =
  | { type: 'start-fetching' }
  | { type: 'set-turn', isYourTurn: boolean, BOARD_SIZE: number, grid: string[] }
  | { type: 'set-not-your-turn', BOARD_SIZE: number, grid: string[] }
  | { type: 'leave-game' }
  | { type: 'win', BOARD_SIZE: number, grid: string[] }
  | { type: 'error', message: string };

function gameReducer(state: FindGameState, action: FindGameAction): FindGameState {
  switch (action.type) {
    case 'start-fetching':
      return { tag: 'loading' };
    case 'set-not-your-turn':
      return { ...state, tag: 'notYourTurn', boardSize: action.BOARD_SIZE, grid: action.grid };
    case 'set-turn':
      return { tag: 'play', boardSize: action.BOARD_SIZE, grid: action.grid }
    case 'leave-game':
      return { tag: 'leave' };
    case 'win':
      return { ...state, tag: 'win', boardSize: action.BOARD_SIZE, grid: action.grid };
    case 'error':
      return { ...state, tag: 'error', message: action.message };
    default:
      return state;
  }
}




export function Game() {
  const [state, dispatch] = React.useReducer(gameReducer, { tag: 'loading' });
  const currentGameId = useCurrentGameId();
  const navigate = useNavigate();
  const user = useCurrentUser();


  const [isFetching, setIsFetching] = React.useState(false);

  const fetchGame = React.useCallback((currentGameId) => {
    if (isFetching) return;
    setIsFetching(true);
    getGame(parseInt(currentGameId)).then(result => {
      const errorData = result.json as ProblemModel;
      const successData = result.json as unknown as GameOutput;
      if (result.contentType === 'application/problem+json') {
        dispatch({ type: 'error', message: errorData.detail });
        setIsFetching(false);
      } else if (result.contentType === 'application/vnd.siren+json') {
        if (successData.class.find((c) => c == 'game') != undefined) {
          if (successData.properties.state.name == 'finished') {
            dispatch({ type: 'win', BOARD_SIZE: successData.properties.variant.boardSize, grid: successData.properties.board.grid });
          } else {
            const isYourTurn = successData.properties.board.turn.player == "W" ? successData.properties.hostId == user.id : successData.properties.guestId == user.id;
            if (isYourTurn) {
              dispatch({ type: 'set-turn', isYourTurn: isYourTurn, BOARD_SIZE: successData.properties.variant.boardSize, grid: successData.properties.board.grid });
            } else {
              dispatch({ type: 'set-not-your-turn', BOARD_SIZE: successData.properties.variant.boardSize, grid: successData.properties.board.grid });
            }
            setIsFetching(false);
          }
        }
      }
    });
  }, [isFetching, user]);


  React.useEffect(() => {
    if (state.tag == 'loading' && !isFetching) {
      fetchGame(currentGameId);
    }
    if (state.tag === 'notYourTurn') {
      const interval = setInterval(() => {
        fetchGame(currentGameId);
      }, 10000);

      return () => clearInterval(interval);

    }

  }, [setIsFetching, isFetching, state.tag, currentGameId, fetchGame, user]);






  const [isMoveInProgress, setIsMoveInProgress] = React.useState(false);

  const handleIntersectionClick = (rowIndex, colIndex, size, grid) => {
    if (isMoveInProgress || rowIndex === 0 || colIndex === 0 || rowIndex === size || colIndex === size) {
      return;
    }
    setIsMoveInProgress(true);
    const colLetter = columnIndexToLetter(colIndex - 1);
    moVe(currentGameId, colLetter, rowIndex, size, grid)
  };


  const moVe = (gameId: number, col: string, row: number, size:number, grid:string[]) => {
    makeMove(gameId, { col: col, row: row }).then(result => {
      const errorData = result.json as ProblemModel;

      if (result.contentType === 'application/problem+json') {
        if (errorData.detail.includes('The game with id') && errorData.detail.includes('is already finished')) {
          dispatch({ type: 'win', BOARD_SIZE: size, grid: grid });
          setIsMoveInProgress(false);
        } else {
          dispatch({ type: 'error', message: errorData.detail });
          setIsMoveInProgress(false);
        }

      } else if (result.contentType === 'application/vnd.siren+json') {
        const successData = result.json as unknown as GameOutput;
        const entities = successData.entities as Entity<GameProperties>[];
        if (successData.class.find((c) => c == 'game') != undefined) {
          const isYourTurn = entities[0].properties.board.turn.player == "W" ? entities[0].properties.hostId == user.id : entities[0].properties.guestId == user.id;
          if (isYourTurn) {
            setIsMoveInProgress(false);
            dispatch({ type: 'set-turn', isYourTurn: isYourTurn, BOARD_SIZE: entities[0].properties.variant.boardSize, grid: entities[0].properties.board.grid });
          } else {
            setIsMoveInProgress(false);
            dispatch({ type: 'set-not-your-turn', BOARD_SIZE: entities[0].properties.variant.boardSize, grid: entities[0].properties.board.grid });
          }
        }
      }
    });
  };

  const handleLeaveGame = (gameId) => {
    setIsFetching(false);
    exitGame(gameId).then(result => {
      const errorData = result.json as ProblemModel;
      if (result.contentType === 'application/problem+json') {
        dispatch({ type: 'error', message: errorData.detail });
      } else if (result.contentType === 'application/vnd.siren+json') {
        dispatch({ type: 'leave-game' });
        navigate('/games');
      }
    })
  };




  switch (state.tag) {
    case 'loading':
      return <div>Loading game...</div>;
    case 'notYourTurn':
      return <div>
        {renderBoard(state.boardSize, cellSize, state.grid, null)}
        Turn: Not your turn
        Player: {user.username}
        <button onClick={() => handleLeaveGame(currentGameId)}>Leave Game</button>
      </div>;
    case 'play':
      return <>
        {renderBoard(state.boardSize, cellSize, state.grid, handleIntersectionClick)}
        Turn: Your turn
        Player: {user.username}
        <button onClick={() => handleLeaveGame(currentGameId)}>Leave Game</button>
      </>;
    case 'leave':
      return <div>You have left the game.</div>;
    case 'win':
      return <>
        {renderBoard(state.boardSize, cellSize, state.grid, null)}
        You won the game!
        Player: {user.username}
        <button onClick={() => navigate('/games')}>Start New Game</button>
      </>;
  }
}


const boardStyle = (boardSize): React.CSSProperties => ({
    position: 'relative',
    width: `${boardSize * cellSize}px`,
    height: `${boardSize * cellSize}px`,
    backgroundImage: `
      linear-gradient(to right, black 1px, transparent 1px),
      linear-gradient(to bottom, black 1px, transparent 1px)
    `,
    backgroundSize: `${cellSize}px ${cellSize}px`,
    boxShadow: `inset 0 -1px 0 0 black, inset -1px 0 0 0 black`,
    boxSizing: 'content-box',
});

function parseGrid(grid) {
    const parsedGrid = {};
    grid.forEach(cell => {
        const [position, player] = cell.split('-');
        const colLetter = position.charAt(0);
        const row = parseInt(position.slice(1), 10) - 1;
        const colIndex = colLetter.charCodeAt(0) - 'a'.charCodeAt(0) + 1;
        const intersectionId = `${row}-${colIndex}`;
        parsedGrid[intersectionId] = player === 'w' ? 'W' : 'B';
    });
    return parsedGrid;
}





function renderBoard(boardSize: number, cellSize: number, grid: string[], handleIntersectionClick: (rowIndex: number, colIndex: number, size: number, grid: string[]) => void) {
  const parsedGrid = parseGrid(grid);
  return (
    <div style={boardStyle(boardSize)}>
      {[...Array(boardSize + 1)].map((_, rowIndex) => (
        [...Array(boardSize + 1)].map((_, colIndex) => {
          const intersectionId = `${rowIndex}-${colIndex}`;
          const isEdge = rowIndex === 0 || colIndex === 0 || rowIndex === boardSize || colIndex === boardSize;
          const letter = parsedGrid[intersectionId];

                            const intersectionStyle: React.CSSProperties = {
                                position: 'absolute',
                                width: '10px',
                                height: '10px',
                                left: `${colIndex * cellSize - 5}px`,
                                top: `${rowIndex * cellSize - 5}px`,
                                cursor: 'pointer',
                                pointerEvents: isEdge ? 'none' : 'auto',
                            };

                            const pieceStyle: React.CSSProperties = {
                                position: 'absolute',
                                fontSize: '20px',
                                lineHeight: '20px',
                                left: '50%',
                                top: '50%',
                                transform: 'translate(-50%, -50%)',
                                userSelect: 'none',
                            };

          return (
            <div
              key={intersectionId}
              style={intersectionStyle}
              onClick={handleIntersectionClick ? () => handleIntersectionClick(rowIndex, colIndex, boardSize, grid) : undefined}
            >
              {(letter === 'W' || letter === 'B') && (
                <span style={pieceStyle}>{letter}</span>
              )}
            </div>
          );
        })
      ))}
    </div>);
}
