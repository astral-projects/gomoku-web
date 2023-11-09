create schema dbo;

create table dbo.Users
(
    id                  int generated always as identity primary key,
    username            varchar(64) unique not null,
    email               varchar(64) unique not null,
    password_validation varchar(256)       not null,
    constraint email_is_valid check (email ~ '^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$'),
    constraint username_min_length check (char_length(username) >= 5),
    constraint username_max_length check (char_length(username) <= 30)
);

create table dbo.Tokens
(
    token_validation varchar(256) primary key,
    created_at       bigint not null default extract(epoch from now()),
    last_used_at     bigint not null default extract(epoch from now()),
    user_id          int references dbo.Users (id) on delete cascade on update cascade,
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
    games_drawn  int not null default 0,
    foreign key (user_id) references dbo.Users (id) on delete cascade on update cascade,
    constraint points_are_valid check ( points >= 0 ),
    constraint games_played_are_valid check ( games_played >= 0 ),
    constraint games_won_are_valid check ( games_won >= 0 ),
    constraint games_won_is_less_than_games_played check ( games_won <= games_played ),
    constraint games_drawn_are_valid check ( games_drawn >= 0 ),
    constraint games_drawn_is_less_than_games_played check ( games_drawn <= games_played )
);

create table dbo.GameVariants
(
    id           serial primary key,
    name         varchar(64) unique not null,
    opening_rule varchar(64)        not null,
    board_size   varchar(64)        not null
);

create table dbo.Lobbies
(
    id         int generated always as identity,
    -- variant_id and host_id could be unique individually, but then
    -- we would need to catch the exception in the code
    host_id    int references dbo.Users (id) on delete cascade on update cascade,
    variant_id int references dbo.GameVariants (id) on delete cascade on update cascade,
    primary key (id, host_id)
);

create table dbo.Games
(
    id                         int generated always as identity primary key,
    state                      varchar(64) check (state in ('IN_PROGRESS', 'FINISHED')) not null,
    variant_id                 int                                                      not null,
    board                      jsonb                                                    not null,
    created_at                 int                                                      not null default extract(epoch from now()),
    updated_at                 int                                                      not null default extract(epoch from now()),
    host_id                    int references dbo.Users (id),
    guest_id                   int references dbo.Users (id),
    lobby_id                   int unique                                               not null,
    foreign key (variant_id) references dbo.GameVariants (id) on delete cascade on update cascade,
    constraint host_and_guest_are_different check (host_id != guest_id),
    constraint created_before_updated check (created_at <= updated_at),
    constraint created_is_valid check (created_at > 0),
    constraint updated_is_valid check (updated_at > 0)
);