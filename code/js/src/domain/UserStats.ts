import { Email, Id, Username } from "./User";

export type UserStats = {
  id: Id;
  username: Username;
  email: Email;
  points: Points;
  rank: Rank;
  gamesPlayed: GamesPlayed;
  wins: Wins;
  draws: Draws;
  losses: Losses;
};

export type Points = {
  value: number;
};

export type Rank = {
  value: number;
};

export type GamesPlayed = {
  value: number;
};

export type Wins = {
  value: number;
};

export type Draws = {
  value: number;
};

export type Losses = {
  value: number;
};
