select * from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = 1;
SELECT * FROM dbo.Games WHERE id = 1 AND (host_id = 10 OR guest_id = 10);

UPDATE dbo.Games
SET host_id = 6
WHERE host_id = 1 AND id = 1;

Delete from dbo.Games where id = 14;
DELETE from dbo.lobbies where id = 9;

select * from (select host_id,guest_id from dbo.games WHERE id=1) where host_id!=3 OR guest_id!=3;

SELECT
    CASE
        WHEN host_id != 3 THEN host_id
        WHEN guest_id != 3 THEN guest_id
        END as user_id
FROM
    dbo.games
WHERE
        id = 1;

SELECT stats.points, stats.games_drawn, stats.games_played, stats.games_won, rank() over(order by points desc) as rank, users.id, users.username, users.email
FROM dbo.Statistics AS stats
         INNER JOIN dbo.Users AS users ON stats.user_id = users.id
WHERE stats.points <= (SELECT points FROM dbo.Statistics WHERE user_id = 3)
ORDER BY stats.points DESC
offset 0
limit 2;


select id, username, email, points, rank() over(order by points desc) as rank, games_played, games_won, games_drawn
from dbo.Users as users
inner join dbo.Statistics as stats
on users.id = stats.user_id
offset :offset
limit :limit;
