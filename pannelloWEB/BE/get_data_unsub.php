<?php
session_start();

if (!isset($_SESSION['logged_in']) || $_SESSION['logged_in'] !== true) {
    http_response_code(403);
    echo json_encode(["error" => "Accesso non autorizzato"]);
    exit;
}
header('Content-Type: application/json');

$host = "localhost";
$user = "root";
$pass = "";
$dbname = "statistiche";

$period = $_GET['period'] ?? 'yesterday';

switch ($period) {
    case 'week':
		$periodo = "Ultima settimana";
        $dateCondition = "date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        break;
    case 'month':
		$periodo = "Ultimo mese";
        $dateCondition = "date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        break;
    case 'year':
		$periodo = "Ultimo anno";
        $dateCondition = "date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
        break;
    case 'all':
		$periodo = "Totale";
        $dateCondition = "1";
        break;
    default:
		$periodo = "ieri";
        $dateCondition = "date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
        break;
}

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    die(json_encode(["error" => "Connessione fallita: " . $conn->connect_error]));
}

$q1 = $conn->query("
    SELECT 
        SUM(unsub_ABB5) AS unsub_ABB5,
        SUM(unsub_ABB10) AS unsub_ABB10,
        SUM(unsub_ABB25) AS unsub_ABB25,
        SUM(unsub_ABB50) AS unsub_ABB50,
        SUM(unsub_ABB100) AS unsub_ABB100
    FROM alphaservice_subscriber
    WHERE $dateCondition;
");

$q2 = $conn->query("
    SELECT 
        SUM(unsub_ABB10) AS unsub_ABB10,
        SUM(unsub_ABB50) AS unsub_ABB50,
        SUM(unsub_ABB100) AS unsub_ABB100
    FROM betaservice_subscriber
    WHERE $dateCondition;
");

$q3 = $conn->query("
    SELECT 
        SUM(unsub_ABB10) AS unsub_ABB10,
        SUM(unsub_ABB50) AS unsub_ABB50,
        SUM(unsub_ABB100) AS unsub_ABB100
    FROM gammaservice_subscriber
    WHERE $dateCondition;
");

function makeChartData($result, $titolo, $service) {
    if (!$result || $result->num_rows === 0) {
        return [
            "titolo" => $titolo,
            "labels" => ["Nessun dato"],
            "values" => [0]
        ];
    }

    $row = $result->fetch_assoc();
	if($service == "alpha"){
		$labels = ["ABB5", "ABB10", "ABB25", "ABB50", "ABB100"];
		$values = [
			(int)$row["unsub_ABB5"],
			(int)$row["unsub_ABB10"],
			(int)$row["unsub_ABB25"],
			(int)$row["unsub_ABB50"],
			(int)$row["unsub_ABB100"]
		];
	}else {
		$labels = ["ABB10", "ABB50", "ABB100"];
		$values = [
			(int)$row["unsub_ABB10"],
			(int)$row["unsub_ABB50"],
			(int)$row["unsub_ABB100"]
		];
	}

    return [
        "titolo" => $titolo,
        "labels" => $labels,
        "values" => $values
    ];
}

$data = [
    "tabella1" => makeChartData($q1, "AlphaService – cancellazoni ".$periodo, "alpha"),
    "tabella2" => makeChartData($q2, "BetaService – cancellazoni ".$periodo, "beta"),
    "tabella3" => makeChartData($q3, "GammaService – cancellazoni ".$periodo, "gamma")
];

$conn->close();
echo json_encode($data);