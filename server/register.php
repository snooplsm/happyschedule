<?php

	try {
		$db = new PDO('sqlite:push.db');
		$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$sql = "insert or replace into USER(push_id) values(?)";
		$stm = $db->prepare($sql);
		$stm->bindParam(1,$_GET["push_id"]);
		$stm->execute();
		$db = null;
	} catch (PDOException $e) {
		echo $e;
		die();
	}
?>
