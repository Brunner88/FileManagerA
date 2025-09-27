package serviceManagement;

import utility.Logging;
import utility.SqlFunction;

import java.io.IOException;
import java.sql.Date;

import static utility.Logging.logLevel.ERROR;

public class Management {

    private final SqlFunction connection;

    private final String service;

    private final Logging logger;

    public Management(SqlFunction connection, String service, Logging logger) {
        this.connection = connection;
        this.service = service;
        this.logger = logger;
    }

    /**
     * gestisce l'apertura di un nuovo record inserendo il nuovo giorno nelle tabelle di dati aggregati
     *
     * @param sqlDate       data in formato sql
     * @param specialManage boolean che definisce se il servizio necessita di una gestione particolare
     */
    public void openDay(Date sqlDate, boolean specialManage) {

        if (!specialManage) {
            connection.insertDate(sqlDate, service + "_subscriber");
            connection.insertDate(sqlDate, service + "_billing");
        } else {
            switch (service) {
                case "betaservice":
                    connection.insertDate(sqlDate, service + "_subscriber");
                    connection.insertDate(sqlDate, service + "_billing");
                    break;
                default:
                    logger.printLog("IL SISTEMA HA RISCONTRATO UN ERRORE NELL'IDENTIFICARE IL SERVIZIO DA ELABORARE", ERROR);
                    break;
            }
        }
    }
    /**
     * Gestisce il manage dei servizi assegnando a ogni servizio la classe di elaborazione appropriata
     *
     * @param report            tipo rapporto (Subscription...)
     * @param fileSystem        locazione del file da elaborare
     * @param nomeFile          nome file da elaborare
     * @param dateToString    data da elaborare in formato string
     * @param sqlDateLastReport data da elaborare in formato sql
     * @param specialManage     boolean che definisce se il servizio necessita di una gestione particolare
     * @return boolean true se la gestione è andata bene, false in caso contrario
     * @throws IOException se non trova il file da gestire
     */
    public boolean manage(String report, String table, String fileSystem, String nomeFile, String dateToString, java.sql.Date sqlDateLastReport, boolean specialManage) throws IOException {

        if (!specialManage) {

            return GenericServiceManagement.gestisci(service, report, table, fileSystem, nomeFile, dateToString, sqlDateLastReport, connection, logger);

        } else {
            switch (service) {
                case "betaservice":
                    return BetaServiceManagement.gestisci(report, table, fileSystem, nomeFile, dateToString, sqlDateLastReport, connection, logger);

                //eventuali altri servizi

                default:
                    logger.printLog("IL SISTEMA HA RISCONTRATO UN ERRORE NELL'IDENTIFICARE IL SERVIZIO DA ELABORARE", ERROR);
                    return false;
            }
        }
    }
    /**
     * Gestisce la chiusura di un record, settando lo stato processed di una riga della tabella subscriber
     *
     * @param date          data in formato yyyy-mm-dd
     * @param specialManage boolean che definisce se il servizio necessita di una gestione particolare
     */
    public void closeDay(String date, boolean specialManage) {

        if (!specialManage) {
            if (connection.isDayComplete(service, date)) {
                String sqlUpdate = "UPDATE `" + service + "_subscriber` SET `processed` = '1'  WHERE `date` = '" + date + "'";
                if (connection.checkExistDate(service + "_subscriber", date)) {
                    connection.executeSqlInsertUpdate(sqlUpdate);
                } else {
                    logger.printLog("Recupero id tabella subscriber per chiusura report giornaliero fallito. " +
                            "La tabella non può essere vuota durante questa operazione", ERROR);
                    System.exit(1);
                }
                sqlUpdate = "UPDATE `" + service + "_billing` SET `processed` = '1'  WHERE `date` = '" + date + "'";
                if (connection.checkExistDate(service + "_billing", date)) {
                    connection.executeSqlInsertUpdate(sqlUpdate);
                } else {
                    logger.printLog("Recupero id tabella billing per chiusura report giornaliero fallito. " +
                            "La tabella non può essere vuota durante questa operazione", ERROR);
                    System.exit(1);
                }
            } else {
                connection.reopenDay(service, date);
            }
        } else {
            //switch preparato nel caso si dovessero aggiungere gestioni particolari
            switch (service) {
                // eventuali altri servizi

                default:
                    logger.printLog("IL SISTEMA NON HA RICONOSCIUTO PER QUALE SERVIZIO COMPLETARE IL GIORNO.", ERROR);
                    break;
            }
        }


    }
}
