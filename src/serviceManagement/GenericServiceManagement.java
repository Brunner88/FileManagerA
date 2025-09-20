package serviceManagement;

import utility.Logging;
import utility.Product;
import utility.ElementCount;
import utility.SqlFunction;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Properties;

import static utility.Logging.logLevel.*;
import static utility.Logging.logLevel.ERROR;
import static utility.Logging.logLevel.WARNING;
import static utility.Utils.*;
import static utility.Utils.containsNull;

public class GenericServiceManagement {

    private static SqlFunction connection;
    private static Logging logger;
    private static Product[] products;
    private static String[] sources;
    private static String[] reasons;
    private static String SERVIZIO = "";

    /**
     * gestione dei report per il servizio generico
     *
     * @param report            tipo di report da processare
     * @param table             tabelle temporanee da lavorare
     * @param fileSystem        locazione del file da elaborare
     * @param nomeFile          nome file da elaborare
     * @param date_to_string    data da elaborare in formato string
     * @param sqlDateLastReport data da elaborare in formato sql
     * @param dbConnection      connessione al db
     * @param fileLogger        log
     * @return boolean contenente esito del processo
     * @throws IOException
     */
    public static boolean gestisci(String service, String report, String table, String fileSystem, String nomeFile, String date_to_string, Date sqlDateLastReport, SqlFunction dbConnection, Logging fileLogger) throws IOException {

        connection = dbConnection;
        logger = fileLogger;
        SERVIZIO = service;

        //caricamento product, source e reason per il servizio
        Properties properties = readProperties(SERVIZIO);
        sources = properties.getProperty("Sources").split(";");
        reasons = properties.getProperty("Reasons").split(";");

        String[] productAndPrice = properties.getProperty("Products").split(";");
        products = new Product[productAndPrice.length];
        int j = 0;
        for (String singleProduct : productAndPrice) {
            String[] tempProduct = singleProduct.split("-");
            products[j] = new Product(tempProduct[0], Integer.parseInt(tempProduct[1]));
            j++;
        }

        //svuoto la tabella temporanea
        connection.svuotaTabella(table);

        //inserisco i dati del csv nella tabella temporanea appropriata
        connection.riempiTabella(table, fileSystem + "/" + nomeFile);

        String logText = "gestito " + report + " del servizio " + SERVIZIO +
                " del " + date_to_string.substring(6, 8) + "/" + date_to_string.substring(4, 6) + "/" +
                date_to_string.substring(0, 4);

        switch (report) {
            case "subscriptions":
                if(gestisciSubscriptions(sqlDateLastReport)){
                    logger.printLog(logText, INFO);
                    return true;
                }
                return false;
            case "unsubscriptions":
                if(gestisciUnsubscription(sqlDateLastReport)) {
                    logger.printLog(logText, INFO);
                    return true;
                }
                return false;
            case "billing":

                return true;
            default:
                logger.printLog("SERVIZIO: " + service + ", REPORT: " + report +
                        ". IL SISTEMA HA RISCONTRATO UN ERRORE NELL'IDENTIFICARE IL FILE DA ELABORARE", FATAL);
                return false;
        }
    }

    /**
     * gestisce la lettura della tabella subscription temporanea e l'inserimento dei dati del rapporto di subscription
     * nella tabella SERVIZIO_subscription
     *
     * @param dataLastReport data in formato sql Date in cui verranno inseriti i dati
     */
    private static boolean gestisciSubscriptions(Date dataLastReport) {

        //conteggio n° sottoscrizioni per prodotto
        String query = "SELECT COUNT(*) as total, `product` " +
                "FROM `subscriptions_temp` " +
                "GROUP BY `product`;";

        List<ElementCount> daySubs = connection.executeSqlQueryElementCount(query, "total", "product");

        //preparo array stringa per inserimento, per la dimensione uso il più piccolo fra la dimensione della lista e i prodotti della config,
        // nel caso nel report fossero presenti più elementi del dovuto
        String[] sqlUpdateProducts = new String[Math.min(daySubs.size(), products.length)];
        int i = 0;
        for (ElementCount ec : daySubs) {
            if (isInProductsArray(ec.getElement(), products)) {
                sqlUpdateProducts[i] = " `sub_" + ec.getElement() + "`= '" + ec.getTotal() + "' ";
                i++;
            } else {
                logger.printLog("Attenzione: nel report subscriptions per il servizio " + SERVIZIO +
                        " è stato trovato un prodotto (" + ec.getElement() + ") non appartenente al servizio. " +
                        "Il prodotto è stato ignorato, se si desidera processarlo aggiornare le tabelle e la configurazione del servizio.", WARNING);
            }
        }

        //conteggio n° per fonte iscrizione per prodotto
        query = "SELECT COUNT(*) as total, `source` " +
                "FROM `subscriptions_temp` " +
                "GROUP BY `source`;";

        List<ElementCount> sourceSubs = connection.executeSqlQueryElementCount(query, "total", "source");

        //preparo array stringa per inserimento, per la dimensione uso il più piccolo fra la dimensione della lista e le fonti della config,
        // nel caso nel report fossero presenti più elementi del dovuto
        String[] sqlUpdateSources = new String[Math.min(sourceSubs.size(), sources.length)];
        i = 0;
        for (ElementCount ec : sourceSubs) {
            if (isInArray(ec.getElement(), sources)) {
                sqlUpdateSources[i] = " `sub_" + ec.getElement() + "`= '" + ec.getTotal() + "' ";
                i++;
            } else {
                logger.printLog("Attenzione: nel report subscriptions per il servizio " + SERVIZIO +
                        " è stata trovata una fonte (" + ec.getElement() + ") non appartenente al servizio. " +
                        "La fonte è stata ignorata, se si desidera processarla aggiornare le tabelle e la configurazione del servizio.", WARNING);
            }
        }

        if(containsNull(sqlUpdateProducts)){
            logger.printLog("ATTENZIONE: i dati dei prodotti di sub ricevuti per il report di subscription per il servizio " + SERVIZIO +
                    " del giorno " + dataLastReport + " non corrispondono alla configurazione", ERROR);
            return false;
        }else if(containsNull(sqlUpdateSources)){
            logger.printLog("ATTENZIONE: i dati delle fonti di sub ricevuti per il report di subscription per il servizio " + SERVIZIO +
                    " del giorno " + dataLastReport + " non corrispondono alla configurazione", ERROR);
            return false;
        }else{
            //inserisco nella tabella
            String sqlUpdateString = "UPDATE `" + SERVIZIO + "_subscriber` SET " +
                    String.join(",", sqlUpdateProducts) + "," +
                    String.join(",", sqlUpdateSources) +
                    "WHERE `date` = '" + dataLastReport + "'";

            connection.executeSqlInsertUpdate(sqlUpdateString);
        }
        return true;
    }

    /**
     * gestisce la lettura della tabella unsubscription temporanea e l'inserimento dei dati del rapporto di unsubscription
     * nella tabella SERVIZIO_unsubscription
     *
     * @param dataLastReport data in formato sql Date in cui verranno inseriti i dati
     */
    private static boolean gestisciUnsubscription(Date dataLastReport) {

        //conteggio n° desottoscrizioni per prodotto
        String query = "SELECT COUNT(*) as total, `product` " +
                "FROM `unsubscriptions_temp` " +
                "GROUP BY `product`;";

        List<ElementCount> dayUnsubs = connection.executeSqlQueryElementCount(query, "total", "product");

        //preparo array stringa per inserimento, per la dimensione uso il più piccolo fra la dimensione della lista e i prodotti della config,
        // nel caso nel report fossero presenti più elementi del dovuto
        String[] sqlUpdateProducts = new String[Math.min(dayUnsubs.size(), products.length)];
        int i = 0;
        for (ElementCount ec : dayUnsubs) {
            if (isInProductsArray(ec.getElement(), products)) {
                sqlUpdateProducts[i] = " `unsub_" + ec.getElement() + "`= '" + ec.getTotal() + "' ";
                i++;
            } else {
                logger.printLog("Attenzione: nel report unsubscriptions per il servizio " + SERVIZIO +
                        " è stato trovato un prodotto (" + ec.getElement() + ") non appartenente al servizio. " +
                        "Il prodotto è stato ignorato, se si desidera processarlo aggiornare le tabelle e la configurazione del servizio.", WARNING);
            }
        }

        //conteggio n° per fonte di desottoscrizione per prodotto
        query = "SELECT COUNT(*) as total, `source` " +
                "FROM `unsubscriptions_temp` " +
                "GROUP BY `source`;";

        List<ElementCount> sourceUnsubs = connection.executeSqlQueryElementCount(query, "total", "source");

        //preparo array stringa per inserimento, per la dimensione uso il più piccolo fra la dimensione della lista e le fonti della config,
        // nel caso nel report fossero presenti più elementi del dovuto
        String[] sqlUpdateSources = new String[Math.min(sourceUnsubs.size(), sources.length)];
        i = 0;
        for (ElementCount ec : sourceUnsubs) {
            if (isInArray(ec.getElement(), sources)) {
                sqlUpdateSources[i] = " `unsub_" + ec.getElement() + "`= '" + ec.getTotal() + "' ";
                i++;
            } else {
                logger.printLog("Attenzione: nel report unsubscriptions per il servizio " + SERVIZIO +
                        " è stata trovata una fonte (" + ec.getElement() + ") non appartenente al servizio. " +
                        "La fonte è stata ignorata, se si desidera processarla aggiornare le tabelle e la configurazione del servizio.", WARNING);
            }
        }

        //conteggio motivazioni desottoscrizione
        query = "SELECT COUNT(*) as total, `reason` " +
                "FROM `unsubscriptions_temp` " +
                "GROUP BY `reason`;";

        List<ElementCount> reasonsUnsubs = connection.executeSqlQueryElementCount(query, "total", "reason");

        //preparo array stringa per inserimento, per la dimensione uso il più piccolo fra la dimensione della lista e le motivazioni della config,
        // nel caso nel report fossero presenti più elementi del dovuto
        String[] sqlUpdateReasons = new String[Math.min(reasonsUnsubs.size(), reasons.length)];
        i = 0;
        for (ElementCount ec : reasonsUnsubs) {
            if (isInArray(ec.getElement(), reasons)) {
                if ("".equals(ec.getElement())) {
                    sqlUpdateReasons[i] = " `unsub_UNKNOWN`= '" + ec.getTotal() + "' ";
                } else {
                    sqlUpdateReasons[i] = " `unsub_" + ec.getElement() + "`= '" + ec.getTotal() + "' ";
                }
                i++;
            } else {
                logger.printLog("Attenzione: nel report unsubscriptions per il servizio " + SERVIZIO +
                        " è stata trovata una motivazione (" + ec.getElement() + ") non appartenente al servizio. " +
                        "La motivazione è stata ignorata, se si desidera processarla aggiornare le tabelle e la configurazione del servizio.", WARNING);
            }
        }

        if(containsNull(sqlUpdateProducts)){
            logger.printLog("ATTENZIONE: i dati dei prodotti di unsub ricevuti per il report di subscription per il servizio " + SERVIZIO +
                    " del giorno " + dataLastReport + " non corrispondono alla configurazione", ERROR);
            return false;
        }else if(containsNull(sqlUpdateSources)){
            logger.printLog("ATTENZIONE: i dati delle fonti di unsub ricevuti per il report di subscription per il servizio " + SERVIZIO +
                    " del giorno " + dataLastReport + " non corrispondono alla configurazione", ERROR);
            return false;
        }else if(containsNull(sqlUpdateReasons)){
            logger.printLog("ATTENZIONE: i dati delle motivazioni di unsub ricevuti per il report di subscription per il servizio " + SERVIZIO +
                    " del giorno " + dataLastReport + " non corrispondono alla configurazione", ERROR);
            return false;
        }else{
            //inserisco nella tabella
            String sqlUpdateString = "UPDATE `" + SERVIZIO + "_subscriber` SET " +
                    String.join(",", sqlUpdateProducts) + "," +
                    String.join(",", sqlUpdateSources) + "," +
                    String.join(",", sqlUpdateReasons) +
                    "WHERE `date` = '" + dataLastReport + "'";

            connection.executeSqlInsertUpdate(sqlUpdateString);
        }
        return true;
    }
}
