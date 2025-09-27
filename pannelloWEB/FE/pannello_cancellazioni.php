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
  <title>Statistiche a doppio livello – PHP + Chart.js</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>
    body { font-family: system-ui, sans-serif; background:#f5f5f5; margin:20px; }
    h1 { text-align:center; }
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
      gap: 20px;
      justify-items: center;
    }
    .chart-box {
      background:#fff;
      padding:16px;
      border-radius:10px;
      box-shadow:0 3px 8px rgba(0,0,0,0.1);
      width:100%;
      max-width:480px;
    }
    canvas { width:100%; height:320px; }
    .controls {
      display:flex;
      justify-content:space-between;
      align-items:center;
      margin-bottom:10px;
    }
    select {
      padding:4px 8px;
      border-radius:5px;
      border:1px solid #ccc;
      background:#f8f8f8;
    }
    h2.section-title {
      grid-column: 1 / -1;
      text-align: center;
      margin: 30px 0 10px;
      color: #333;
    }
  </style>
</head>
<body>
  <h1>Statistiche Abbonamenti</h1>
  <div id="menu"></div>
  <script src="menu.js"></script>

  <h2 class="section-title">Totale Cancellazioni</h2>
  <div id="chartsMain" class="charts-grid"></div>

  <h2 class="section-title">Fonte cancellazioni</h2>
  <div id="chartsFrom" class="charts-grid"></div>
  
  <h2 class="section-title">Motivo cancellazioni</h2>
  <div id="chartsFor" class="charts-grid"></div>

  <script>
    function drawPieChart(container, data) {
      const canvas = document.createElement("canvas");
      container.appendChild(canvas);
      return new Chart(canvas, {
        type: "pie",
        data: {
          labels: data.labels,
          datasets: [{
            data: data.values,
            backgroundColor: [
              "#4e79a7", "#f28e2b", "#e15759", "#76b7b2",
              "#59a14f", "#edc948", "#b07aa1", "#ff9da7"
            ],
            borderColor: "#fff",
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          plugins: {
            title: { display: true, text: data.titolo, font: { size: 18 } },
            legend: { position: "bottom" }
          }
        }
      });
    }

    function createChartBox(label, fetchFn) {
      const chartBox = document.createElement("div");
      chartBox.classList.add("chart-box");

      const controls = document.createElement("div");
      controls.classList.add("controls");

      const title = document.createElement("strong");
      title.textContent = label;

      const select = document.createElement("select");
      const options = [
        { value: "yesterday", text: "Ieri" },
        { value: "week", text: "Ultima settimana" },
        { value: "month", text: "Ultimo mese" },
        { value: "year", text: "Ultimo anno" },
        { value: "all", text: "Da sempre" }
      ];
      options.forEach(opt => {
        const o = document.createElement("option");
        o.value = opt.value;
        o.textContent = opt.text;
        select.appendChild(o);
      });
      select.value = "yesterday";

      controls.appendChild(title);
      controls.appendChild(select);
      chartBox.appendChild(controls);

      const canvasWrapper = document.createElement("div");
      chartBox.appendChild(canvasWrapper);

      let chartInstance = null;
      async function updateChart() {
        const data = await fetchFn(select.value);
        canvasWrapper.innerHTML = "";
        chartInstance = drawPieChart(canvasWrapper, data);
      }

      select.addEventListener("change", updateChart);
      updateChart();

      return chartBox;
    }

    async function getDataSub(period="yesterday") {
      const res = await fetch(`../BE/get_data_unsub.php?period=${period}`);
      return await res.json();
    }

    async function getDataSubFrom(period="yesterday") {
      const res = await fetch(`../BE/get_data_unsub_from.php?period=${period}`);
      return await res.json();
    }
	
	async function getDataSubFor(period="yesterday") {
      const res = await fetch(`../BE/get_data_unsub_for.php?period=${period}`);
      return await res.json();
    }

    async function main() {
      const containerMain = document.getElementById("chartsMain");
      const containerFrom = document.getElementById("chartsFrom");
	  const containerFor = document.getElementById("chartsFor");

      const labels = {
        tabella1: "AlphaService",
        tabella2: "BetaService",
        tabella3: "GammaService"
      };

      const dbData = await getDataSub("yesterday");
      for (const key of Object.keys(dbData)) {
        const chartBox = createChartBox(labels[key], async (period) => {
          const data = await getDataSub(period);
          return data[key];
        });
        containerMain.appendChild(chartBox);
      }

      const dbDataFrom = await getDataSubFrom("yesterday");
      for (const key of Object.keys(dbDataFrom)) {
        const chartBox = createChartBox(labels[key], async (period) => {
          const data = await getDataSubFrom(period);
          return data[key];
        });
        containerFrom.appendChild(chartBox);
      }
	  
      const dbDataFor = await getDataSubFor("yesterday");
      for (const key of Object.keys(dbDataFor)) {
        const chartBox = createChartBox(labels[key], async (period) => {
          const data = await getDataSubFor(period);
          return data[key];
        });
        containerFor.appendChild(chartBox);
      }
    }

    main();
  </script>
</body>
</html>
