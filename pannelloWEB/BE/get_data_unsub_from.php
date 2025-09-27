<?php
session_start();

if (!isset($_SESSION['logged_in']) || $_SESSION['logged_in'] !== true) {
    http_response_code(403); // accesso vietato
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
        SUM(unsub_storeA) AS unsub_storeA,
        SUM(unsub_storeB) AS unsub_storeB,
        SUM(unsub_storeC) AS unsub_storeC,
        SUM(unsub_online) AS unsub_online,
        SUM(unsub_representative) AS unsub_representative,
		SUM(unsub_tricky) AS unsub_tricky
    FROM alphaservice_subscriber
    WHERE $dateCondition;
");

$q2 = $conn->query("
    SELECT 
        SUM(unsub_storeA) AS unsub_storeA,
        SUM(unsub_storeB) AS unsub_storeB,
        SUM(unsub_storeC) AS unsub_storeC,
		SUM(unsub_storeD) AS unsub_storeD,
		SUM(unsub_storeE) AS unsub_storeE,
        SUM(unsub_online) AS unsub_online,
        SUM(unsub_representative) AS unsub_representative,
		SUM(unsub_tricky) AS unsub_tricky
    FROM betaservice_subscriber
    WHERE $dateCondition;
");

$q3 = $conn->query("
    SELECT 
        SUM(unsub_storeA) AS unsub_storeA,
        SUM(unsub_storeB) AS unsub_storeB,
        SUM(unsub_storeC) AS unsub_storeC,
		SUM(unsub_storeD) AS unsub_storeD,
		SUM(unsub_storeE) AS unsub_storeE,
        SUM(unsub_online) AS unsub_online,
        SUM(unsub_representative) AS unsub_representative,
		SUM(unsub_tricky) AS unsub_tricky
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
		$labels = ["A", "B", "C", "online", "representative", "tricky"];
		$values = [
			(int)$row["unsub_storeA"],
			(int)$row["unsub_storeB"],
			(int)$row["unsub_storeC"],
			(int)$row["unsub_online"],
			(int)$row["unsub_representative"],
			(int)$row["unsub_tricky"]
		];
	}else {
		$labels = ["A", "B", "C", "D", "E", "online", "representative", "tricky"];
		$values = [
			(int)$row["unsub_storeA"],
			(int)$row["unsub_storeB"],
			(int)$row["unsub_storeC"],
			(int)$row["unsub_storeD"],
			(int)$row["unsub_storeE"],
			(int)$row["unsub_online"],
			(int)$row["unsub_representative"],
			(int)$row["unsub_tricky"]
		];
	}

    return [
        "titolo" => $titolo,
        "labels" => $labels,
        "values" => $values
    ];
}

$data = [
    "tabella1" => makeChartData($q1, "AlphaService – Abbonati ".$periodo, "alpha"),
    "tabella2" => makeChartData($q2, "BetaService – Abbonati ".$periodo, "beta"),
    "tabella3" => makeChartData($q3, "GammaService – Abbonati ".$periodo, "gamma")
];

$conn->close();
echo json_encode($data);