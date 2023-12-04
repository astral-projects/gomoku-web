import * as React from "react";

const BOARD_SIZE = 20;
const cellSize = 30;

const boardStyle: React.CSSProperties = {
  position: 'relative',
  width: `${BOARD_SIZE * cellSize}px`,
  height: `${BOARD_SIZE * cellSize}px`,
  backgroundImage: `
      linear-gradient(to right, black 1px, transparent 1px),
      linear-gradient(to bottom, black 1px, transparent 1px)
    `,
  backgroundSize: `${cellSize}px ${cellSize}px`,
  boxShadow: `inset 0 -1px 0 0 black, inset -1px 0 0 0 black`, 
  boxSizing: 'content-box',

};

type IntersectionState = {
  [key: string]: 'T' | null;
};

export function Game() {
  const [intersections, setIntersections] = React.useState<IntersectionState>({});

  const handleIntersectionClick = (rowIndex: number, colIndex: number) => {
    if (rowIndex === 0 || colIndex === 0 || rowIndex === BOARD_SIZE || colIndex === BOARD_SIZE) {
      return;
    }

    const intersectionId = `intersection-${rowIndex}-${colIndex}`;


    setIntersections(prevIntersections => ({
      ...prevIntersections,
      [intersectionId]: 'T'
    }));
  };

  return (
    <div style={boardStyle}>
      {[...Array(BOARD_SIZE + 1)].map((_, rowIndex) => (
        [...Array(BOARD_SIZE + 1)].map((_, colIndex) => {
          const intersectionId = `intersection-${rowIndex}-${colIndex}`;
          const isEdge = rowIndex === 0 || colIndex === 0 || rowIndex === BOARD_SIZE || colIndex === BOARD_SIZE;
  
        
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
              onClick={() => handleIntersectionClick(rowIndex, colIndex)}
            >
              {intersections[intersectionId] === 'T' && (
                <span style={pieceStyle}>
                  T
                </span>
              )}
            </div>
          );
        })
      ))}
    </div>
  );
}