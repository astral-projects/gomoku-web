import * as React from "react";

const cellSize = 30;
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
  
  
  
  
  
  export function renderBoard(boardSize: number, grid: string[], handleIntersectionClick: (rowIndex: number, colIndex: number, size: number, grid: string[]) => void = undefined) {
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
  