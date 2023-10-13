create schema dbo;

create table dbo.User
(
    user_id             int generated always as identity primary key,
    username            varchar(64)        not null,
    email               varchar(64) unique not null,
    password_validation varchar(256)       not null
);

create table dbo.Token
(
    token_validation VARCHAR(256) primary key,
    created_at       bigint not null,
    last_used_at     bigint not null,
    user_id          int references dbo.User (user_id)
);

create table dbo.Statistic
(
    user_id      int primary key,
    points       int not null,
    games_played int not null,
    games_won    int not null,
    foreign key (user_id) references dbo.User (user_id)
);

create table dbo.Lobby
(
    host_id      int primary key,
    variant      varchar(64) references dbo.Variant (variant) not null,
    opening_rule varchar(64) references dbo.Rule (rule)       not null,
    board_size   int references dbo.BoardSize (size)          not null,
    foreign key (host_id) references dbo.User (user_id)
);

create table dbo.Variant
(
    variant VARCHAR(64) primary key,
);

create table dbo.Rule
(
    rule VARCHAR(64) primary key,
);

create table dbo.BoardSize
(
    size int primary key,
);

create table dbo.Game
(
    game_id      serial primary key,
    state        varchar(64) check (state in ('IN-PROGRESS', 'FINISHED')) not null,
    variant      varchar(64) references dbo.Variant (variant)             not null,
    opening_rule varchar(64) references dbo.Rule (rule)                   not null,
    board_size   int references dbo.BoardSize (size)                      not null,
    board        jsonb                                                    not null,
    created      int                                                      not null,
    updated      int                                                      not null,
    userA        int references dbo.User (user_id),
    userB        int references dbo.User (user_id),
);