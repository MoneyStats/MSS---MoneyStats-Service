function getDocumentation() {
  return window.open(window.location.origin + "/documentation/index.html");
}

function setAnchorHref(id) {
    // Ottenere l'URL dell'origine della finestra
    const origin = window.location.origin;
    // Ottenere l'elemento anchor dal documento
    const anchorElement = document.getElementById(id); // Assicurati di sostituire 'ilTuoID' con l'ID reale del tuo elemento anchor
    // Verificare se l'URL dell'origine contiene la parola "stg"
    if (origin.includes('stg')) {
    // Se sì, impostare l'URL desiderato per l'ambiente di staging
    anchorElement.href = 'https://stg.moneystats.app.giovannilamarmora.com';
    } else if (origin.includes('localhost')) {
    // Altrimenti, impostare l'URL di default per localhost
    anchorElement.href = 'http://localhost:4200';
    } else anchorElement.href = 'https://moneystats.github.io/auth/login';
}

setAnchorHref('open_app');
setAnchorHref('open_app2');