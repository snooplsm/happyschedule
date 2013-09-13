CREATE TABLE nested_trip (
        id INT AUTO_INCREMENT PRIMARY KEY,
		trip_id VARCHAR(20) NOT NULL,
        stop_id VARCHAR(20) NOT NULL,
        lft INT NOT NULL,
        rgt INT NOT NULL
);

SELECT parent.trip_id
FROM nested_trip AS node,
        nested_trip AS parent
WHERE node.lft BETWEEN parent.lft AND parent.rgt
        AND node.stop_id = '148'
ORDER BY node.lft;

SELECT parent * FROM nested_trip node where node.lft BETWEEN 