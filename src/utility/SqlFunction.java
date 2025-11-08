package utility;

import java.sql.*;
import java.util.Arrays;

import static utility.Logging.logLevel.DEBUG;
import static utility.Logging.logLevel.FATAL;

public class SqlFunction {

    private final Logging logger;
    private final Connection connection;

    public SqlFunction(Logging logger, String db, String dbUser, String dbPass) {
        this.logger = logger;
        this.connection = accessDB(db, dbUser, dbPass);
    }

    /**
     * Apre la connessione al db
     *
     * @param db     schema da puntare
     * @param dbUser utente
     * @param dbPass password
     * @return Connection object o null in caso di errori
     */
    public Connection accessDB(String db, String dbUser, String dbPass) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.printLog("Library mysql.jdbc not found", FATAL);
            System.exit(1);
        }
        try {
            return DriverManager.getConnection(db, dbUser, dbPass);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Impossibile connecttersi al db", FATAL);
            System.exit(1);
        }
        return null;
    }

    /**
     * Controlla la presenza di un servizio nella tabella last report
     *
     * @param servizio nome del servizio da controllare
     * @return boolean true se il servizio è presente, false altrimenti
     */
    public boolean checkServiceStatus(String servizio) {

        String existSql = "SELECT `servizio` " +
                "FROM `last_report` " +
                "WHERE `servizio` LIKE '" + servizio + "'";

        String esiste = executeSqlQueryString(existSql, "servizio");

        return null != esiste && !"".equalsIgnoreCase(esiste);
    }

    /**
     * Esegue una interrogazione del db (singola colonna)
     *
     * @param sql    la stringa contenente l'istruzione sql da eseguire
     * @param column la colonna da recuperare
     * @return String contentente il valore ottenuto
     */
    public String executeSqlQueryString(String sql, String column) {

        String res = null;

        logger.printLog(sql, DEBUG);

        try {
            Statement stmt = connection.createStatement();

            res = executeStatementQuerySingleString(stmt,sql,column);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Impossibile connecttersi al db", FATAL);
            System.exit(1);
        }

        logger.printLog("Risultato: " + res, DEBUG);

        return res;
    }

    private String executeStatementQuerySingleString(Statement stmt, String sql, String column){
        String res = null;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                res = rs.getString(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Errori nell'esecuzione della query", FATAL);
            System.exit(1);
        }

        return res;
    }

    /**
     * Inizializza la riga del servizio nella tabella last_report
     *
     * @param report          nome del rapporto
     * @param servizio        nome del servizio
     * @param startDateString data da cui partire
     */
    public void insertInLastReport(String[] report, String servizio, String startDateString) {

        String sql = "INSERT INTO `last_report` (`servizio`, `";

        sql += String.join("`, `", report);
        sql += "`) VALUES ('" + servizio + "', '";

        String[] dates = new String[report.length];

        Arrays.fill(dates, startDateString);

        sql += String.join("', '", dates);
        sql += "');";

        executeSqlInsertUpdate(sql);
    }

    /**
     * Esegue un insert o un update nel db
     *
     * @param sql la stringa contenente l'istruzione sql da eseguire
     */
    public void executeSqlInsertUpdate(String sql) {

        try {
            logger.printLog("executeSqlInsertUpdate: " + sql, DEBUG);

            Statement stmt = connection.createStatement();

            executeStatmentUpdate(stmt, sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Impossibile connecttersi al db", FATAL);
            System.exit(1);
        }
    }

    private void executeStatmentUpdate(Statement stmt, String sql){
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Errori nell'esecuzione della query", FATAL);
            System.exit(1);
        }
    }
}
