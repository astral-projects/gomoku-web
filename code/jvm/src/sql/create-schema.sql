create schema dbo;

create table dbo.Users
(
    id                  int generated always as identity primary key,
    username            varchar(64) unique not null,
    email               varchar(64) unique not null,
    password_validation varchar(256)       not null,
    constraint email_is_valid check (email ~ '^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$'),
    -- TODO: if necessary, add a password regex constraint
    constraint username_min_length check (char_length(username) >= 5),
    constraint username_max_length check (char_length(username) <= 30)
);

create table dbo.Tokens
(
    token_validation varchar(256) primary key,
    created_at       bigint not null default extract(epoch from now()),
    last_used_at     bigint not null default extract(epoch from now()),
    user_id          int references dbo.Users (id),
    constraint created_before_last_used check (created_at <= last_used_at),
    constraint created_at_is_valid check (created_at > 0),
    constraint last_used_at_is_valid check (last_used_at > 0)
);

create table dbo.Statistics
(
    user_id      int primary key,
    points       int not null default 0,
    games_played int not null default 0,
    games_won    int not null default 0,
    foreign key (user_id) references dbo.Users (id),
    constraint points_are_valid check ( points >= 0 ),
    constraint games_played_are_valid check ( games_played >= 0 ),
    constraint games_won_are_valid check ( games_won >= 0 ),
    constraint games_won_is_less_than_games_played check ( games_won <= games_played )
);

create table dbo.GameVariants
(
    variant varchar(64) primary key
);

create table dbo.OpeningRules
(
    rule varchar(64) primary key
);

create table dbo.BoardSizes
(
    size int primary key
);

create table dbo.Lobbies
(
    host_id      int primary key,
    game_variant varchar(64) references dbo.GameVariants (variant) not null,
    opening_rule varchar(64) references dbo.OpeningRules (rule)    not null,
    board_size   int references dbo.BoardSizes (size)              not null,
    foreign key (host_id) references dbo.Users (id)
);

create table dbo.Games
(
    id           int generated always as identity primary key,
    state        varchar(64) check (state in ('IN_PROGRESS', 'FINISHED')) not null,
    game_variant varchar(64) references dbo.GameVariants (variant)        not null,
    opening_rule varchar(64) references dbo.OpeningRules (rule)           not null,
    board_size   int references dbo.BoardSizes (size)                     not null,
    board        jsonb                                                    not null,
    -- TODO: add board json constraints once we have a board representation in domain
    created_at   int                                                      not null default extract(epoch from now()),
    updated_at   int                                                      not null default extract(epoch from now()),
    host_id      int references dbo.Users (id),
    guest_id     int references dbo.Users (id),
    constraint host_and_guest_are_different check (host_id != guest_id),
    constraint created_before_updated check (created_at <= updated_at),
    constraint created_is_valid check (created_at > 0),
    constraint updated_is_valid check (updated_at > 0)
);


UPDATE dbo.Games
SET host_id = 10
WHERE host_id = 1 AND id = 1;
