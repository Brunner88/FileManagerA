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
        SUM(sub_storeA) AS sub_storeA,
        SUM(sub_storeB) AS sub_storeB,
        SUM(sub_storeC) AS sub_storeC,
        SUM(sub_online) AS sub_online,
        SUM(sub_representative) AS sub_representative,
		SUM(sub_tricky) AS sub_tricky
    FROM alphaservice_subscriber
    WHERE $dateCondition;
");

$q2 = $conn->query("
    SELECT 
        SUM(sub_storeA) AS sub_storeA,
        SUM(sub_storeB) AS sub_storeB,
        SUM(sub_storeC) AS sub_storeC,
		SUM(sub_storeD) AS sub_storeD,
		SUM(sub_storeE) AS sub_storeE,
        SUM(sub_online) AS sub_online,
        SUM(sub_representative) AS sub_representative,
		SUM(sub_tricky) AS sub_tricky
    FROM betaservice_subscriber
    WHERE $dateCondition;
");

$q3 = $conn->query("
    SELECT 
        SUM(sub_storeA) AS sub_storeA,
        SUM(sub_storeB) AS sub_storeB,
        SUM(sub_storeC) AS sub_storeC,
		SUM(sub_storeD) AS sub_storeD,
		SUM(sub_storeE) AS sub_storeE,
        SUM(sub_online) AS sub_online,
        SUM(sub_representative) AS sub_representative,
		SUM(sub_tricky) AS sub_tricky
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
			(int)$row["sub_storeA"],
			(int)$row["sub_storeB"],
			(int)$row["sub_storeC"],
			(int)$row["sub_online"],
			(int)$row["sub_representative"],
			(int)$row["sub_tricky"]
		];
	}else {
		$labels = ["A", "B", "C", "D", "E", "online", "representative", "tricky"];
		$values = [
			(int)$row["sub_storeA"],
			(int)$row["sub_storeB"],
			(int)$row["sub_storeC"],
			(int)$row["sub_storeD"],
			(int)$row["sub_storeE"],
			(int)$row["sub_online"],
			(int)$row["sub_representative"],
			(int)$row["sub_tricky"]
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