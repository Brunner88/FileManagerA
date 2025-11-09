package utility;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utility.Logging.logLevel.*;

public class SqlFunction {

    private final Logging logger;
    private final Connection connection;
    private static final  String RESULT = "Risultato: ";
    private static final String IMPOSSIBLE = "Impossibile ottenere i dati per la query ";
    private static final String NO_CONNECTION = "Impossibile connecttersi al db";
    private static final String PROCESSED = "processed";

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
            logger.printLog(NO_CONNECTION, FATAL);
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
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }

        logger.printLog(RESULT + res, DEBUG);

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
            logger.printLog(NO_CONNECTION, FATAL);
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

    /**
     * controlla tutti i rapporti della tabella last_report per un servizio e torna la data più vecchia
     *
     * @param servizio nome del servizio
     * @return String contente la data in formato yyyyMMdd
     */
    public String readOldestDateString(String servizio) {
        int res = 0;

        String sql = "SELECT `subscriptions`,`unsubscriptions`,`billing` " +
                "FROM `last_report` " +
                "WHERE `servizio` = '" + servizio + "'";

        logger.printLog("readOldestDateString: " + sql, DEBUG);

        try {

            Statement stmt = connection.createStatement();

            res = readOldestDateStringExecute(stmt, sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }

        logger.printLog(RESULT + res, DEBUG);

        return "" + res;
    }

    private int readOldestDateStringExecute(Statement stmt, String sql){
        int res = 0;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            //scorro il resultset e salvo solo da data più vecchia
            while (rs.next()) {

                int[] result = {
                        Integer.parseInt(rs.getString("subscriptions")),
                        Integer.parseInt(rs.getString("unsubscriptions")),
                        Integer.parseInt(rs.getString("billing"))
                };

                int j = 0;

                for (int i = 0; i < result.length; i++) {

                    if (result[i] <= result[j]) {
                        res = result[i];
                        j = i;
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(IMPOSSIBLE + sql, FATAL);
            System.exit(1);
        }
        return  res;
    }

    /**
     * Chiude la connessione al db
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

    /**
     * legge l'ultima data processata per un report per un servizio
     *
     * @param report   nome del report (subscription, unsubscription...)
     * @param servizio nome del servizio
     * @return int che contiene la data formattata (yyyyMMdd) dell'ultimo report processato per il servizio
     */
    public int readDateString(String report, String servizio) {

        String sql = "SELECT `" + report + "` " +
                "FROM `last_report` " +
                "WHERE `servizio` = '" + servizio + "' LIMIT 1";

        String res = executeSqlQueryString(sql, report);

        return res != null ? Integer.parseInt(res) : 0;
    }

    /**
     * Controlla se esiste una determinata data nella tabella SERVIZIO_subscriber e nella tabella SERVIZIO_billing
     *
     * @param servizio nome del servizio
     * @param data     data da cercare (formato yyyyMMdd)
     * @return boolean true se presente, false altrimenti
     */
    public boolean existDate(String servizio, String data) {

        int sub = executeSqlQueryInt("SELECT COUNT(`date`) as found " +
                "FROM `" + servizio + "_subscriber` " +
                "WHERE `date` LIKE '" + data + "'", "found");

        int bill = executeSqlQueryInt("SELECT COUNT(`date`) as found " +
                "FROM `" + servizio + "_billing` " +
                "WHERE `date` LIKE '" + data + "'", "found");

        if (sub != bill) {
            logger.printLog("Errore le tabelle " + servizio + "_subscriber e " + servizio + "_billing presentano date disallineate", FATAL);
            System.exit(1);
        }

        return sub != 0;

    }

    /**
     * Esegue una interrogazione del db (singola colonna, singola riga)
     *
     * @param sql    la query da eseguire
     * @param column la colonna nella quale cercare il dato aggregato
     * @return int contentente il valore ottenuto
     */
    public int executeSqlQueryInt(String sql, String column) {

        int res = 0;
        try {
            logger.printLog("executeSqlQueryInt: " + sql, DEBUG);
            Statement stmt = connection.createStatement();

            res = executeStatementQuerySingleInt(stmt, sql, column);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }

        logger.printLog(RESULT + res, DEBUG);
        return res;
    }

    private int executeStatementQuerySingleInt(Statement stmt, String sql, String column){
        int res = 0;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                res = rs.getInt(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(IMPOSSIBLE + sql, ERROR);
            System.exit(1);
        }

        return res;
    }

    /**
     * inserisce la data nella tabella tab
     *
     * @param data la data da inserire
     * @param tab  la tabella in cui inserire
     */
    public void insertDate(Date data, String tab) {

        executeSqlInsertUpdate("INSERT INTO `" + tab + "` (`date`) VALUES ('" + data + "') ");

    }

    /**
     * Controlla se una determinata data nella tabella SERVIZIO_subscriber e nella tabella SERVIZIO_billing è stata già processata
     *
     * @param servizio nome del servizio
     * @param data     data da cercare (formato yyyyMMdd)
     * @return boolean true se presente, false altrimenti
     */
    public boolean alreadyProcessed(String servizio, String data) {

        int sub = executeSqlQueryInt("SELECT processed " +
                "FROM `" + servizio + "_subscriber` " +
                "WHERE `date` LIKE '" + data + "'", PROCESSED);

        int bill = executeSqlQueryInt("SELECT processed " +
                "FROM `" + servizio + "_billing` " +
                "WHERE `date` LIKE '" + data + "'", PROCESSED);

        return sub == 1 && bill == 1;
    }

    /**
     * Imposta una riga della tabella SERVIZIO_subscriber e della tabella SERVIZIO_billing come non processate (processed = 2)
     *
     * @param servizio         nome servizio
     * @param dataDaProcessare data da settare a non processata in formato yyyy-mm-dd
     */
    public void setAsNotProcessed(String servizio, String dataDaProcessare) {

        executeSqlInsertUpdate("UPDATE `" + servizio + "_subscriber` " +
                "SET `processed` = '2'  " +
                "WHERE `date` = '" + dataDaProcessare + "'"
        );

        executeSqlInsertUpdate("UPDATE `" + servizio + "_billing` " +
                "SET `processed` = '2'  " +
                "WHERE `date` = '" + dataDaProcessare + "'"
        );
    }

    /**
     * Aggiorna la tabella last report impostando il giorno passato per il servizio su quel rapporto
     *
     * @param report           rapporto
     * @param servizio         servizio
     * @param dataDaProcessare data in formato yyyyMMdd
     */
    public void updateLastReport(String report, String servizio, String dataDaProcessare) {

        executeSqlInsertUpdate("UPDATE `last_report` " +
                "SET `" + report + "` = '" + dataDaProcessare + "' " +
                "WHERE `servizio` = '" + servizio + "';"
        );
    }

    /**
     * legge tutte le date di un servizio dalla tabella last_report e controlla se sono tutte allineate alla più recente
     *
     * @param report nome del report (subscription, unsubscription...)
     * @return boolean contenente true se nono allineate, false altrimenti
     */
    public boolean confrontaDataString(String servizio, String report) {

        String res = "";

        String sql = "SELECT `subscriptions`,`unsubscriptions`,`billing` " +
                "FROM `last_report` " +
                "WHERE `servizio` = '" + servizio + "'";

        logger.printLog("confrontaDataString: " + sql, DEBUG);

        try {
            Statement stmt = connection.createStatement();

            res = confrontaDataStringExecute(stmt, sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }

        String[] split = res.split("-");

        boolean flag = true;
        int val = readDateString(report, servizio);
        for (int j = 1; j < split.length && flag; j++) {
            if (Integer.parseInt(split[j]) < val)
                flag = false;
        }

        return flag;
    }

    private String confrontaDataStringExecute(Statement stmt, String sql){

        String res = "";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                res = rs.getString("subscriptions") + "-" + rs.getString("unsubscriptions") + "-" + rs.getString("billing");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(IMPOSSIBLE + sql, ERROR);
            System.exit(1);
        }

        return res;
    }

    /**
     * controlla se le righe della tabella servizio_subscriber e della tabella servizio_billing sono entrambe completabili ( != 2)
     *
     * @param servizio il servizio da controllare
     * @param data     la data da controllare
     * @return boolean true se righe entrambe diverse da 2, false altrimenti
     */
    public boolean isDayComplete(String servizio, String data) {

        int sub = executeSqlQueryInt("SELECT processed " +
                "FROM `" + servizio + "_subscriber` " +
                "WHERE `date` LIKE '" + data + "'", PROCESSED);

        int bill = executeSqlQueryInt("SELECT processed " +
                "FROM `" + servizio + "_billing` " +
                "WHERE `date` LIKE '" + data + "'", PROCESSED);

        return sub != 2 && bill != 2;

    }

    /**
     * Controlla se esiste una riga con la data specificata nella tabella
     * @param tabella tabella da controllare
     * @param data data da cercare in formato yyyy-mm-dd
     * @return boolean true se esiste, false altrimenti
     */
    public boolean checkExistDate(String tabella, String data) {

        String sql = "SELECT `id_sub` " +
                "FROM `" + tabella + "` " +
                "WHERE `date` = '" + data + "'";

        int res = executeSqlQueryInt(sql, "id_sub");

        return res > 0;
    }

    /**
     * Imposta le righe della tabella servizio_subscriber e della tabella servizio_billing segnate come non processate a 0 (rendendole riprocessabili in futuro)
     *
     * @param servizio nome servizio
     * @param data     data da impostare in formato yyyy-mm-dd
     */
    public void reopenDay(String servizio, String data) {

        executeSqlInsertUpdate("UPDATE `" + servizio + "_subscriber` " +
                "SET `processed` = '0'  " +
                "WHERE `date` = '" + data + "'"
        );

        executeSqlInsertUpdate("UPDATE `" + servizio + "_billing` " +
                "SET `processed` = '0'  " +
                "WHERE `date` = '" + data + "'"
        );
    }

    /**
     * svuota la tabella tab
     *
     * @param tab tabella da svuotare
     */
    public void svuotaTabella(String tab) {

        try {
            Statement stmt = connection.createStatement();

            String sql = "TRUNCATE " + tab;

            logger.printLog("svuotaTabella: " + sql, DEBUG);

            svuotaTabellaExecute(stmt, sql, tab);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }
    }

    /**
     * riempe la tabella tab con i dati prelevati dal path specificato
     *
     * @param tab  tabella da riempire
     * @param path il path del file da cui prelevare i dati
     */
    public void riempiTabella(String tab, String path) {

        try {
            Statement stmt = connection.createStatement();

            String sql = "LOAD data LOCAL INFILE  '" + path + "' " +
                    "INTO TABLE " + tab + " " +
                    "FIELDS TERMINATED BY  ',' " +
                    "LINES TERMINATED BY  '\n' " +
                    "IGNORE 1 LINES";

            logger.printLog("Load csv in mySql: " + sql, DEBUG);

            riempiTabellaExecute(stmt, tab, sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }
    }

    private void svuotaTabellaExecute(Statement stmt, String sql, String tab){
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Errore nello svuotamento della tabella " + tab, ERROR);
            System.exit(1);
        }
    }

    private void riempiTabellaExecute(Statement stmt, String tab, String sql){
        try {
            stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog("Errore nel caricamento della tabella " + tab, ERROR);
            System.exit(1);
        }
    }

    /**
     * Esegue una interrogazione del db (due colonne, più righe)
     *
     * @param sql la query da eseguire
     * @param columnOne la prima colonna da recuperare (deve tornare un int)
     * @param columnTwo la seconda colonna da recuperare (deve tornare una stringa)
     * @return lista di oggetti ElementCount recuperata
     */
    public List<ElementCount> executeSqlQueryElementCount(String sql, String columnOne, String columnTwo) {

        List<ElementCount> res = new ArrayList<>();

        try {
            logger.printLog("executeSqlQueryElementCount: " + sql, DEBUG);
            Statement stmt = connection.createStatement();

            res = executeStatementElementCount(stmt, sql, columnOne, columnTwo);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(NO_CONNECTION, FATAL);
            System.exit(1);
        }

        logger.printLog(RESULT + res, DEBUG);
        return res;
    }

    private List<ElementCount> executeStatementElementCount(Statement stmt, String sql, String columnOne, String columnTwo){

        List<ElementCount> res = new ArrayList<>();

        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int total = rs.getInt(columnOne);
                String element = rs.getString(columnTwo);
                res.add(new ElementCount(element, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.printLog(IMPOSSIBLE + sql, ERROR);
            System.exit(1);
        }

        return res;
    }
}
