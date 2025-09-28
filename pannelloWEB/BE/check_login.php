<?php
session_start();

$VALID_USER = "admin";
$VALID_PASS = "password123";

$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

if ($username === $VALID_USER && $password === $VALID_PASS) {
    $_SESSION['logged_in'] = true;
    $_SESSION['username'] = $username;
    header("Location: ../FE/pannello_andamenti.php");
    exit;
} else {
    header("Location: login.html?error=Credenziali%20non%20valide");
    exit;
}
