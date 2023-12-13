export type GameEntities = {
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
      turn?: {
        player: string;
        timeLeftInSec: {
          value: number;
        };
      };
      winner?: string;
    };
    createdAt: string;
    updatedAt: string;
    hostId: number;
    guestId: number;
  };