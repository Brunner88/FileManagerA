// menu.js
(async () => {
  // carica HTML del menu
  const res = await fetch("menu.html");
  const html = await res.text();
  document.getElementById("menu").innerHTML = html;

  // carica CSS del menu (una sola volta)
  if (!document.getElementById("menu-style")) {
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "menu.css";
    link.id = "menu-style";
    document.head.appendChild(link);
  }

  // evidenzia la pagina attiva
  const links = document.querySelectorAll("#menu a");
  links.forEach(link => {
    if (link.href === window.location.href) {
      link.classList.add("active");
    }
  });
})();
