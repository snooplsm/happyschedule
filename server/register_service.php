<?php

	try {
		$db = new PDO('sqlite:push.db');
		$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$sql = "select id from USER where push_id=?";
		$stm = $db->prepare($sql);
		$stm->bindParam(1,$_GET["push_id"]);
		$stm->execute();
		$row = $stm->fetch();
		$userId = $row["id"];
		echo $userId;
		if($userId!=null) {
			error_log($_POST["services"],1);
			$req = array_merge($_GET, $_POST);
			$services = json_decode($req["services"],true);
			//{"NJTRANSIT_ACRL":{"1":[]},"NJTRANSIT_NLR":{"1":[3,4,5,6,7,8,9,10]}}
			$sql = "insert or replace into SERVICES(user_id,screenname,day,hour) values(?,?,?,?)";
			$stm = $db->prepare("delete from SERVICES where user_id=?");
			$stm->bindParam(1,$userId);
			$stm->execute();
			$stm = $db->prepare($sql);
			foreach($services as $screenname => $service) {
	//			echo $key,'\n';
				foreach($service as $day => $hours) {
					foreach($hours as $hour) {
						$stm->bindParam(1,$userId);
						$stm->bindParam(2,$screenname);
						$stm->bindParam(3,$day);
						$stm->bindParam(4,$hour);
						$stm->execute();					
					}
				}
			}
		} else {
			echo "bad";
			die();
		}
		$db = null;
	} catch (PDOException $e) {
		echo $e;
		die();
	}
?>
