package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import serviceManagement.Management;
import utility.Logging;
import utility.RetriveCSV;
import utility.SqlFunction;
import utility.Utils;

import static utility.Utils.*;

import static utility.Logging.logLevel.*;
import static utility.Utils.readProperties;

public class Main {
    public static void main(String[] args) throws IOException, SQLException, ParseException, ClassNotFoundException {
        Properties serviceProperties = readServicesProperties();
        String[] services = serviceProperties.getProperty("services").split(";");

        Logging logger = new Logging(serviceProperties.getProperty("logLevel"));
        logger.printLog("Inizio processo gestione", DEBUG);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String endDateString = getIeri(sdf);

        for (String servizio : services) {
            processService(servizio, sdf, endDateString, logger);
        }

    }

    private static String getIeri(SimpleDateFormat sdf) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return sdf.format(calendar.getTime());
    }

    private static void processService(String servizio, SimpleDateFormat sdf, String endDateString,
                                       Logging logger)
            throws IOException, SQLException, ParseException, ClassNotFoundException {

        logger.printLog("Inizio gestione servizio " + servizio, INFO);

        Properties props = readProperties(servizio);
        String startDateString = props.getProperty("primaData");

        SqlFunction db = new SqlFunction(logger,
                props.getProperty("db"),
                props.getProperty("dbUser"),
                props.getProperty("dbPass")
        );

        if (!db.checkServiceStatus(servizio)) {
            db.insertInLastReport(props.getProperty("Report").split(";"), servizio, startDateString);
        }

        Management manage = new Management(db, servizio, logger);
        String lastProcessed = db.readOldestDateString(servizio);
        boolean specialManage = Boolean.parseBoolean(props.getProperty("SpecialManage"));

        if (Integer.parseInt(lastProcessed) >= Integer.parseInt(endDateString)) {
            logger.printLog("Servizio " + servizio + " ha già processato tutte le date disponibili.", ERROR);
            return;
        }

        for (String report : props.getProperty("Report").split(";")) {

            logger.printLog("Inizio gestione files di " + report + " per " + servizio, INFO);

            String ultimoReport = getPrimaData(servizio, report, startDateString, db, logger, sdf);
            Date startingDate = sdf.parse(ultimoReport);

            while (Integer.parseInt(ultimoReport) <= Integer.parseInt(endDateString)) {
                gestisciGiornata(servizio, report, props, db, manage, logger, specialManage, sdf, startingDate);
                startingDate = Utils.alterDate(startingDate, 1);
                ultimoReport = sdf.format(startingDate);
            }
        }

        db.closeConnection();
    }

    private static String getPrimaData(String servizio, String report, String startDateString,
                                       SqlFunction db, Logging logger, SimpleDateFormat sdf) throws ParseException {
        int dbDate = db.readDateString(report, servizio);
        if (dbDate < 20250901) {
            logger.printLog("Data non valida per " + report + " - uso data di default", WARNING);
            if (Integer.parseInt(startDateString) < 20250901) {
                logger.printLog("Data di default non valida per " + servizio + " - report " + report, FATAL);
                throw new IllegalArgumentException("Data di partenza non valida");
            }
            return startDateString;
        } else {
            Date d = sdf.parse(String.valueOf(dbDate));
            return sdf.format(Utils.alterDate(d, 1));
        }
    }

    private static void gestisciGiornata(String servizio, String report, Properties props, SqlFunction db, Management manage,
                                         Logging logger, boolean specialManage,
                                         SimpleDateFormat sdf, Date startingDate)
            throws SQLException, IOException, ParseException, ClassNotFoundException {

        String dataDaProcessare = sdf.format(startingDate);
        String anno = dataDaProcessare.substring(0, 4);
        String mese = dataDaProcessare.substring(4, 6);
        java.sql.Date sqlDate = new java.sql.Date(startingDate.getTime());
        String dataSql = anno + "-" + mese + "-" + dataDaProcessare.substring(6, 8);

        if (!db.existDate(servizio, dataSql)) manage.openDay(sqlDate, specialManage);

        if (!db.alreadyProcessed(servizio, dataSql)) {
            String fileSystem = props.getProperty("repository") + "/" + report + "/" + anno + "/" + mese;
            String nomeFile = dataDaProcessare + "_" + report + ".csv";

            File file = new File(fileSystem + "/" + nomeFile);
            if (file.exists()) {
                logger.printLog("File già presente: " + file.getAbsolutePath(), WARNING);
                return;
            }

            String url = props.getProperty("BaseUrl") + props.getProperty("ServiceCode") + "/" + nomeFile;
            boolean retrieved = RetriveCSV.retrive(servizio, url, report, fileSystem, dataDaProcessare, logger);

            if (!retrieved) {
                db.setAsNotProcessed(servizio, dataDaProcessare);
                String msg = String.format("Il report %s del %s/%s/%s non è stato recuperato dal repository online.",
                        report, dataDaProcessare.substring(6, 8), dataDaProcessare.substring(4, 6), dataDaProcessare.substring(0, 4));
                logger.printLog(msg, INFO);
            }

            //TODO gestione file
        } else {
            logger.printLog(String.format("Report %s del %s/%s/%s per %s già processato.",
                    report, dataDaProcessare.substring(6, 8), mese, anno, servizio), WARNING);
        }

        //TODO chiusura giorno
    }
}