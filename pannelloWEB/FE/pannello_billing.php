<?php
session_start();
if (!isset($_SESSION['logged_in']) || $_SESSION['logged_in'] !== true) {
    header("Location: login.php");
    exit;
}
?>
<!doctype html>
<html lang="it">
<head>
  <meta charset="utf-8">
  <title>Billing – Analisi</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>
    body { font-family: system-ui, sans-serif; background:#f5f5f5; margin:30px; }
    h1 { text-align:center; margin-bottom:40px; }
    .chart-container {
      background:#fff;
      border-radius:12px;
      padding:20px;
      box-shadow:0 3px 10px rgba(0,0,0,0.1);
      max-width:1000px;
      margin:30px auto;
    }
    canvas { width:100%; height:480px; }
    .controls { text-align:center; margin-bottom:20px; }
    select { padding:6px 12px; border-radius:6px; border:1px solid #ccc; background:#fafafa; }
  </style>
</head>
<body>
  <h1>Billing</h1>
  <div id="menu"></div>
  <script src="menu.js"></script>
  <div class="chart-container">
    <div class="controls">
      <label>Periodo:
        <select id="period">
          <option value="week">Ultima settimana</option>
          <option value="month">Ultimo mese</option>
          <option value="6months">Ultimi 6 mesi</option>
          <option value="year">Ultimo anno</option>
          <option value="all">Da sempre</option>
        </select>
      </label>
    </div>

    <canvas id="billingChart"></canvas>
  </div>

  <div class="chart-container">
    <h2 style="text-align:center;margin-bottom:20px;">Andamento Ricavi</h2>
    <canvas id="trendChart"></canvas>
  </div>

  <script>
    const ctx1 = document.getElementById("billingChart").getContext("2d");
    const ctx2 = document.getElementById("trendChart").getContext("2d");
    let chart1 = null;
    let chart2 = null;

    async function loadData(period = "week") {
      const res = await fetch(`../BE/get_data_billing.php?period=${period}`);
      const json = await res.json();

      const labels = json.histogram.labels;
      const d = json.histogram.datasets;

      const data1 = {
        labels: labels,
        datasets: [
          { label: "Lordo", backgroundColor: "#4e79a7", data: d.gross },
          { label: "Pagamenti totali", backgroundColor: "#59a14f", data: d.full },
          { label: "Pagamenti parziali", backgroundColor: "#f28e2b", data: d.partial },
          { label: "Nessun pagamento", backgroundColor: "#e15759", data: d.failed }
        ]
      };

      const options1 = {
        responsive: true,
        scales: {
          x: { stacked: false },
          y: {
            beginAtZero: true,
            title: { display: true, text: "Billing" }
          }
        },
        plugins: {
          title: {
            display: true,
            text: "Operazioni per tipo di abbonamento",
            font: { size: 18 }
          }
        }
      };

      if (chart1) chart1.destroy();
      chart1 = new Chart(ctx1, { type: "bar", data: data1, options: options1 });

      const dates = json.trend.labels;
      const expect = json.trend.expect;
      const real = json.trend.real;

      const data2 = {
        labels: dates,
        datasets: [
          {
            label: "Incasso atteso",
            data: expect,
            borderColor: "#f28e2b",
            fill: false,
            tension: 0.2,
            pointRadius: 3
          },
          {
            label: "Incasso reale",
            data: real,
            borderColor: "#4e79a7",
            fill: false,
            tension: 0.2,
            pointRadius: 3
          }
        ]
      };

      const options2 = {
        responsive: true,
        scales: {
          x: { title: { display: true, text: "Data" } },
          y: { beginAtZero: true, title: { display: true, text: "Ricavi (€)" } }
        },
        plugins: {
          title: {
            display: true,
            text: "Andamento dei ricavi nel tempo",
            font: { size: 18 }
          }
        }
      };

      if (chart2) chart2.destroy();
      chart2 = new Chart(ctx2, { type: "line", data: data2, options: options2 });
    }

    document.getElementById("period").addEventListener("change", (e) => {
      loadData(e.target.value);
    });

    loadData();
  </script>
</body>
</html>
