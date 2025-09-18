package serviceManagement;

import utility.Logging;
import utility.SqlFunction;

import java.io.IOException;
import java.sql.Date;

import static utility.Logging.logLevel.FATAL;

public class GenericServiceManagement {

    private static SqlFunction connection;
    private static Logging logger;
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

        //svuoto la tabella temporanea
        connection.svuotaTabella(table);

        //inserisco i dati del csv nella tabella temporanea appropriata
        connection.riempiTabella(table, fileSystem + "/" + nomeFile);

        switch (report) {
            case "subscriptions":

                return false;
            case "unsubscriptions":

                return false;
            case "billing":

                return true;
            default:
                logger.printLog("SERVIZIO: " + service + ", REPORT: " + report +
                        ". IL SISTEMA HA RISCONTRATO UN ERRORE NELL'IDENTIFICARE IL FILE DA ELABORARE", FATAL);
                return false;
        }
    }
}
