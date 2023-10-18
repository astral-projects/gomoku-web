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