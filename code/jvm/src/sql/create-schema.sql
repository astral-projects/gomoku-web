create schema dbo;

create table dbo.User
(
    user_id             int generated always as identity primary key,
    username            varchar(64) unique not null,
    email               varchar(64) unique not null,
    password_validation varchar(256)       not null,
    constraint email_is_valid check (email ~ '^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$'),
    -- TODO: if necessary, add a password regex constraint
    constraint password_min_length check (char_length(password_validation) >= 8),
    constraint password_max_length check (char_length(password_validation) <= 40),
    constraint username_min_length check (char_length(username) >= 5),
    constraint username_max_length check (char_length(username) <= 30)
);

create table dbo.Token
(
    token_validation varchar(256) primary key,
    created_at       bigint not null,
    last_used_at     bigint not null,
    user_id          int references dbo.User (user_id),
    constraint created_before_last_used check (created_at <= last_used_at),
    constraint created_at_is_valid check (created_at > 0),
    constraint last_used_at_is_valid check (last_used_at > 0)
);

create table dbo.Statistic
(
    user_id      int primary key,
    points       int not null default 0,
    games_played int not null default 0,
    games_won    int not null default 0,
    foreign key (user_id) references dbo.User (user_id),
    constraint points_are_valid check ( points >= 0 ),
    constraint games_played_are_valid check ( games_played >= 0 ),
    constraint games_won_are_valid check ( games_won >= 0 ),
    constraint games_won_is_less_than_games_played check ( games_won <= games_played )
);

create table dbo.GameVariant
(
    variant VARCHAR(64) primary key
);

create table dbo.OpeningRule
(
    rule VARCHAR(64) primary key
);

create table dbo.BoardSize
(
    size int primary key
);

create table dbo.Lobby
(
    host_id      int primary key,
    game_variant varchar(64) references dbo.GameVariant (variant) not null,
    opening_rule varchar(64) references dbo.OpeningRule (rule)    not null,
    board_size   int references dbo.BoardSize (size)              not null,
    foreign key (host_id) references dbo.User (user_id)
);

create table dbo.Game
(
    game_id      int generated always as identity primary key,
    state        varchar(64) check (state in ('IN_PROGRESS', 'FINISHED')) not null,
    game_variant varchar(64) references dbo.GameVariant (variant)         not null,
    opening_rule varchar(64) references dbo.OpeningRule (rule)            not null,
    board_size   int references dbo.BoardSize (size)                      not null,
    board        jsonb                                                    not null,
    -- TODO: add board json constraints once we have a board representation in domain
    created      int                                                      not null,
    updated      int                                                      not null,
    host         int references dbo.User (user_id),
    guest        int references dbo.User (user_id),
    constraint host_and_guest_are_different check (host != guest) ,
    constraint created_before_updated check (created <= updated),
    constraint created_is_valid check (created > 0),
    constraint updated_is_valid check (updated > 0)
);