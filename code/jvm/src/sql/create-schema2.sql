create schema dbo;

create table dbo.User(
    user_id int generated always as identity primary key,
    username VARCHAR(64) not null,
    email VARCHAR(64) unique not null,
    password_validation VARCHAR(256) not null
);

create table dbo.Token(
    token_validation VARCHAR(256) primary key,
    created_at bigint not null,
    last_used_at bigint not null,
    user_id int references dbo.User(user_id)
);

create table dbo.Statistic(
    user_id int primary key,
    rank int not null,
    points int not null,
    games_played int not null,
    games_won int not null,
    foreign key (user_id) references dbo.User(user_id)
);

create table dbo.Lobby(
    lobby_id serial,
    variant varchar(64) check (variant in ('FREESTYLE', 'RENJU', 'CARO', 'OMOK', 'NINUKI-RENJU', 'PENTE')) not null,
    opening_rule varchar(64) check (opening_rule in ('PRO', 'LONG-PRO', 'SWAP', 'SWAP2')) not null,
    joined_at timestamp not null, --it is really need?
    user1 int references dbo.User(user_id),
    user2 int references dbo.User(user_id),
    primary key(lobby_id, user)
);

create table dbo.Game(
    game_id serial,
    state VARCHAR(64) check (state in ('inProgress', 'Finished')) not null,
    board jsonb not null,
    size int not null,
    lobby int references dbo.Lobby(lobby_id),
    user int references dbo.User(user_id),
    primary key(game_id, lobby, user)
);


