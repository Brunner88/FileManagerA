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
  <title>Andamenti Sub / Unsub</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <link rel="stylesheet" href="menu.css">
  <style>
    body { font-family: system-ui, sans-serif; background:#f5f5f5; margin:0; padding:0; }
    h1 { text-align:center; margin:30px 0; }
    .chart-grid {
      display:block;
      gap:30px;
      max-width:1200px;
      margin:auto;
      padding:20px;
    }
    .chart-box {
      background:#fff;
      border-radius:12px;
      box-shadow:0 3px 10px rgba(0,0,0,0.1);
      padding:20px;
	  margin: 20px 0;
    }
    canvas { width:100%; height:320px; }
    .controls {
      text-align:center;
      margin:10px 0 20px;
    }
    select {
      padding:6px 10px;
      border-radius:6px;
      border:1px solid #ccc;
      background:#fafafa;
    }
  </style>
</head>
<body>
  <div id="menu"></div>
  <h1>Andamento Iscrizioni e Disiscrizioni</h1>

  <div class="controls">
    <label>Periodo:
      <select id="period">
        <option value="week">Ultima settimana</option>
        <option value="month" selected>Ultimo mese</option>
        <option value="6months">Ultimi 6 mesi</option>
        <option value="year">Ultimo anno</option>
        <option value="all">Da sempre</option>
      </select>
    </label>
  </div>

  <div id="chartsContainer" class="chart-grid"></div>

  <script src="menu.js"></script>
  <script>
    const container = document.getElementById("chartsContainer");
    const services = {
      alpha: "AlphaService",
      beta: "BetaService",
      gamma: "GammaService"
    };

    let charts = {};

    async function getData(period="month") {
      const res = await fetch(`../BE/get_data_andamenti.php?period=${period}`);
      return await res.json();
    }

    function createLineChart(container, label, labels, subs, unsubs) {
      const box = document.createElement("div");
      box.className = "chart-box";
      const title = document.createElement("h3");
      title.textContent = label;
      title.style.textAlign = "center";
      box.appendChild(title);
      const canvas = document.createElement("canvas");
      box.appendChild(canvas);
      container.appendChild(box);

      const ctx = canvas.getContext("2d");
      return new Chart(ctx, {
        type: "line",
        data: {
          labels: labels,
          datasets: [
            {
              label: "Sub",
              data: subs,
              borderColor: "#4e79a7",
              fill: false,
              tension: 0.2
            },
            {
              label: "Unsub",
              data: unsubs,
              borderColor: "#e15759",
              fill: false,
              tension: 0.2
            }
          ]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: "bottom" },
            title: { display: true, text: "Andamento giornaliero" }
          },
          scales: {
            y: { beginAtZero: true, title: { display: true, text: "Utenti" } },
            x: { title: { display: true, text: "Data" } }
          }
        }
      });
    }

    async function updateCharts(period="month") {
      const data = await getData(period);
      container.innerHTML = "";
      for (const [key, label] of Object.entries(services)) {
        const s = data[key];
        charts[key] = createLineChart(container, label, s.labels, s.sub, s.unsub);
      }
    }

    document.getElementById("period").addEventListener("change", e => {
      updateCharts(e.target.value);
    });

    updateCharts();
  </script>
</body>
</html>
