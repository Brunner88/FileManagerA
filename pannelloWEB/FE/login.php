<!doctype html>
<html lang="it">
<head>
  <meta charset="utf-8">
  <title>Login - AlphaService Dashboard</title>
  <style>
    body {
      font-family: system-ui, sans-serif;
      background: #1a1a1a;
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
    }
    form {
      background: #222;
      padding: 30px;
      border-radius: 10px;
      box-shadow: 0 3px 10px rgba(0,0,0,0.5);
      width: 300px;
    }
    h2 { text-align:center; }
    input {
      width: 93%;
      padding: 10px;
      margin: 8px 0;
      border-radius: 6px;
      border: none;
    }
    button {
      width: 100%;
      padding: 10px;
      border: none;
      border-radius: 6px;
      background: #4ea3ff;
      color: white;
      font-weight: bold;
      cursor: pointer;
    }
    .error { color: #ff6b6b; text-align: center; margin-bottom: 10px; }
  </style>
</head>
<body>
  <form action="../BE/check_login.php" method="POST">
    <h2>Accesso statistiche servizi</h2>
    <?php if (isset($_GET['error'])) echo '<div class="error">'.htmlspecialchars($_GET['error']).'</div>'; ?>
    <input type="text" name="username" placeholder="Username" required>
    <input type="password" name="password" placeholder="Password" required>
    <button type="submit">Entra</button>
  </form>
</body>
</html>
