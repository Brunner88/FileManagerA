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

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    die(json_encode(["error" => "Connessione fallita: " . $conn->connect_error]));
}

$period = $_GET['period'] ?? 'week';
switch ($period) {
    case 'week':
        $dateCondition = "`date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        break;
    case 'month':
        $dateCondition = "`date` >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        break;
    case '6months':
        $dateCondition = "`date` >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
        break;
    case 'year':
        $dateCondition = "`date` >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";
        break;
    case 'all':
        $dateCondition = "1";
        break;
    default:
        $dateCondition = "`date` = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
        break;
}

$qHistogram = $conn->query("
SELECT 
    SUM(gross_ABB5) AS gross_ABB5, SUM(full_ABB5) AS full_ABB5, SUM(partial_ABB5) AS partial_ABB5, SUM(failed_ABB5) AS failed_ABB5,
    SUM(gross_ABB10) AS gross_ABB10, SUM(full_ABB10) AS full_ABB10, SUM(partial_ABB10) AS partial_ABB10, SUM(failed_ABB10) AS failed_ABB10,
    SUM(gross_ABB25) AS gross_ABB25, SUM(full_ABB25) AS full_ABB25, SUM(partial_ABB25) AS partial_ABB25, SUM(failed_ABB25) AS failed_ABB25,
    SUM(gross_ABB50) AS gross_ABB50, SUM(full_ABB50) AS full_ABB50, SUM(partial_ABB50) AS partial_ABB50, SUM(failed_ABB50) AS failed_ABB50,
    SUM(gross_ABB100) AS gross_ABB100, SUM(full_ABB100) AS full_ABB100, SUM(partial_ABB100) AS partial_ABB100, SUM(failed_ABB100) AS failed_ABB100
FROM alphaservice_billing
WHERE $dateCondition;
");

$row = $qHistogram->fetch_assoc();
$labels = ["ABB5", "ABB10", "ABB25", "ABB50", "ABB100"];
$data = ["gross" => [], "full" => [], "partial" => [], "failed" => []];

foreach ($labels as $abb) {
    $data["gross"][] = (int)$row["gross_$abb"];
    $data["full"][] = (int)$row["full_$abb"];
    $data["partial"][] = (int)$row["partial_$abb"];
    $data["failed"][] = (int)$row["failed_$abb"];
}

$qTrend = $conn->query("
SELECT 
    `date`, 
    SUM(expect_revenue) AS expect_revenue,
    SUM(real_revenue) AS real_revenue
FROM alphaservice_billing
WHERE $dateCondition
GROUP BY `date`
ORDER BY `date` ASC;
");

$dates = [];
$expect = [];
$real = [];
while ($r = $qTrend->fetch_assoc()) {
    $dates[] = $r['date'];
    $expect[] = (float)$r['expect_revenue'];
    $real[] = (float)$r['real_revenue'];
}

$conn->close();

echo json_encode([
    "histogram" => [
        "labels" => $labels,
        "datasets" => $data
    ],
    "trend" => [
        "labels" => $dates,
        "expect" => $expect,
        "real" => $real
    ]
]);
