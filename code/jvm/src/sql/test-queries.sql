select * from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = 1;
SELECT * FROM dbo.Games WHERE id = 1 AND (host_id = 10 OR guest_id = 10);

UPDATE dbo.Games
SET host_id = 6
WHERE host_id = 1 AND id = 1;

Delete from dbo.Games where id = 8;
DELETE from dbo.lobbies where host_id = 7;

