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
        SUM(unsub_NNFNZ) AS unsub_NNFNZ,
        SUM(unsub_TSLW) AS unsub_TSLW,
        SUM(unsub_TPRC) AS unsub_TPRC,
        SUM(unsub_CHNG) AS unsub_CHNG,
        SUM(unsub_PGRD) AS unsub_PGRD,
		SUM(unsub_UNKNOWN) AS unsub_UNKNOWN
    FROM alphaservice_subscriber
    WHERE $dateCondition;
");

$q2 = $conn->query("
    SELECT 
        SUM(unsub_NNFNZ) AS unsub_NNFNZ,
        SUM(unsub_TSLW) AS unsub_TSLW,
        SUM(unsub_TPRC) AS unsub_TPRC,
        SUM(unsub_CHNG) AS unsub_CHNG,
        SUM(unsub_PGRD) AS unsub_PGRD,
		SUM(unsub_UNKNOWN) AS unsub_UNKNOWN
    FROM betaservice_subscriber
    WHERE $dateCondition;
");

$q3 = $conn->query("
    SELECT 
        SUM(unsub_NNFNZ) AS unsub_NNFNZ,
        SUM(unsub_TSLW) AS unsub_TSLW,
        SUM(unsub_TPRC) AS unsub_TPRC,
        SUM(unsub_CHNG) AS unsub_CHNG,
        SUM(unsub_PGRD) AS unsub_PGRD,
		SUM(unsub_UNKNOWN) AS unsub_UNKNOWN
    FROM gammaservice_subscriber
    WHERE $dateCondition;
");

function makeChartData($result, $titolo) {
    if (!$result || $result->num_rows === 0) {
        return [
            "titolo" => $titolo,
            "labels" => ["Nessun dato"],
            "values" => [0]
        ];
    }

    $row = $result->fetch_assoc();
	$labels = ["NNFNZ", "TSLW", "TPRC", "CHNG", "PGRD", "UNKNOWN"];
	$values = [
		(int)$row["unsub_NNFNZ"],
		(int)$row["unsub_TSLW"],
		(int)$row["unsub_TPRC"],
		(int)$row["unsub_CHNG"],
		(int)$row["unsub_PGRD"],
		(int)$row["unsub_UNKNOWN"]
	];

    return [
        "titolo" => $titolo,
        "labels" => $labels,
        "values" => $values
    ];
}

$data = [
    "tabella1" => makeChartData($q1, "AlphaService – Abbonati ".$periodo,),
    "tabella2" => makeChartData($q2, "BetaService – Abbonati ".$periodo),
    "tabella3" => makeChartData($q3, "GammaService – Abbonati ".$periodo)
];

$conn->close();
echo json_encode($data);