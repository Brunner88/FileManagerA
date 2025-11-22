<?php
session_start();
if (!isset($_SESSION['logged_in']) || $_SESSION['logged_in'] !== true) {
    header("Location: login.php");
    exit;
}
header('Content-Type: application/json');

$host = "localhost";
$user = "root";
$pass = "";
$dbname = "statistiche";
$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    die(json_encode(["error" => "Connessione fallita: " . $conn->connect_error]));
}

$period = $_GET['period'] ?? 'month';
switch ($period) {
    case 'week':
        $dateCond = "`date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        break;
    case '6months':
        $dateCond = "`date` >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
        break;
    case 'year':
        $dateCond = "`date` >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
        break;
    case 'all':
        $dateCond = "1";
        break;
    default:
        $dateCond = "`date` >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        break;
}

function getServiceData($conn, $table, $dateCond) {
	if($table = "alphaservice_subscriber"){
		$q = $conn->query("
			SELECT `date`, 
				   (sub_ABB5 + sub_ABB10 + sub_ABB25 + sub_ABB50 + sub_ABB100) AS sub_total,
				   (unsub_ABB5 + unsub_ABB10 + unsub_ABB25 + unsub_ABB50 + unsub_ABB100) AS unsub_total 
			FROM $table
			WHERE $dateCond
			GROUP BY `date`
			ORDER BY `date` ASC;
		");
	}else{
		$q = $conn->query("
			SELECT `date`, 
				   (sub_ABB10 + sub_ABB50 + sub_ABB100) AS sub_total,
				   (unsub_ABB10 + unsub_ABB50 + unsub_ABB100) AS unsub_total 
			FROM $table
			WHERE $dateCond
			GROUP BY `date`
			ORDER BY `date` ASC;
		");
	}
    $dates = []; $subs = []; $unsubs = [];
    while ($r = $q->fetch_assoc()) {
        $dates[] = $r['date'];
        $subs[] = (int)$r['sub_total'];
        $unsubs[] = (int)$r['unsub_total'];
    }
    return ["labels" => $dates, "sub" => $subs, "unsub" => $unsubs];
}

$data = [
    "alpha" => getServiceData($conn, "alphaservice_subscriber", $dateCond),
    "beta"  => getServiceData($conn, "betaservice_subscriber", $dateCond),
    "gamma" => getServiceData($conn, "gammaservice_subscriber", $dateCond)
];

$conn->close();
echo json_encode($data);
