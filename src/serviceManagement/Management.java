package serviceManagement;

import utility.Logging;
import utility.SqlFunction;

import java.sql.Date;
import java.sql.SQLException;

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
     * @throws SQLException
     */
    public void openDay(Date sqlDate, boolean specialManage) throws SQLException {

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
}
