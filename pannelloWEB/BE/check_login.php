<?php
session_start();

$host = "localhost";
$user = "root";
$pass = "";
$dbname = "statistiche";

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die(json_encode(["error" => "Connessione fallita: " . $conn->connect_error]));
}

$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

$stmt = $conn->prepare("SELECT username, password FROM utenti WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();

    if (md5($password) === $row['password']) {

        $_SESSION['logged_in'] = true;
        $_SESSION['username'] = $username;

        header("Location: ../FE/pannello_andamenti.php");
        exit;
    }
}

header("Location: login.html?error=Credenziali%20non%20valide");
exit;
