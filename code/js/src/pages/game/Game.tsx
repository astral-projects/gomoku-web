import * as React from "react";

const BOARD_SIZE = 15;
const cellSize = 30;
/*
const BOARD_SIZE = 15;

const createEmptyBoard = () => {
  return Array.from({ length: BOARD_SIZE }, () => Array(BOARD_SIZE).fill(null));
};

export function Game() {
  const [board, setBoard] = React.useState(createEmptyBoard());

  const handleCellClick = (rowIndex:number, colIndex:number) => { 
    const updatedBoard = board.map((row, rIdx) => 
      row.map((cell, cIdx) => {
        if (rIdx === rowIndex && cIdx === colIndex) {
          return 'X';
        }
        return cell;
      })
    );
    setBoard(updatedBoard);
  };

   const cellSize = 30; // Definido como 30px para este exemplo

   const boardStyle:React.CSSProperties = {
    position: 'relative',
    width: `${BOARD_SIZE * cellSize}px`,
    height: `${BOARD_SIZE * cellSize}px`,
    backgroundImage: `
      linear-gradient(to right, black 1px, transparent 1px),
      linear-gradient(to bottom, black 1px, transparent 1px)
    `,
    backgroundSize: `${cellSize}px ${cellSize}px`,
  };

 
const halfCellSize = cellSize / 2; // Metade do tamanho da célula

return (
  <div>
    <h1>Game</h1>
    <div style={boardStyle}>
      {board.map((row, rowIndex) => {
        return row.map((cell, colIndex) => {
          const cellStyle: React.CSSProperties = {
            position: 'absolute',
            width: `${cellSize}px`,
            height: `${cellSize}px`,
            left: `${colIndex * cellSize}px`,
            top: `${rowIndex * cellSize}px`,
          };

          const xStyle: React.CSSProperties = {
            position: 'absolute',
            fontSize: `${cellSize}px`, 
            lineHeight: `${cellSize}px`,
            left: `calc(-50% + ${halfCellSize}px)`,
            top: `calc(-50% + ${halfCellSize}px)`, 
            transform: 'translate(-50%, -50%)', 
            userSelect: 'none', 
          };

          return (
            <div key={`${rowIndex}-${colIndex}`} style={cellStyle} onClick={() => handleCellClick(rowIndex, colIndex)}>
              {cell === 'X' && (
                <span style={xStyle}>
                  X
                </span>
              )}
            </div>
          );
        });
      })}
    </div>
  </div>
);
}*/
const boardStyle:React.CSSProperties = {
    position: 'relative',
    width: `${BOARD_SIZE * cellSize}px`,
    height: `${BOARD_SIZE * cellSize}px`,
    backgroundImage: `
      linear-gradient(to right, black 1px, transparent 1px),
      linear-gradient(to bottom, black 1px, transparent 1px)
    `,
    backgroundSize: `${cellSize}px ${cellSize}px`,
  };

  type IntersectionState = {
    [key: string]: 'X' | null;
  };

export function Game() {
    const [intersections, setIntersections] = React.useState<IntersectionState>({});
  
    const handleIntersectionClick = (intersectionId: string) => {
        setIntersections(prevIntersections => ({
          ...prevIntersections,
          [intersectionId]: 'X'
        }));
      };
  
      return (
        <div style={boardStyle}>
          {}
          {[...Array(BOARD_SIZE)].map((_, rowIndex) => (
            [...Array(BOARD_SIZE)].map((_, colIndex) => {
              const intersectionId = `intersection-${rowIndex}-${colIndex}`;
      
              const intersectionStyle: React.CSSProperties = {
                position: 'absolute', 
                width: '10px', 
                height: '10px',
                left: `${colIndex * cellSize - 5}px`, 
                top: `${rowIndex * cellSize - 5}px`,
                cursor: 'pointer', 
              };
              
              return (
                <div
                  key={intersectionId}
                  style={intersectionStyle}
                  onClick={() => handleIntersectionClick(intersectionId)}
                >
                  {}
                  {intersections[intersectionId] === 'X' ? 'X' : ''}
                </div>
              );
            })
          ))}
        </div>
      );      
  }