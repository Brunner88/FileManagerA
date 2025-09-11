package main;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import serviceManagement.Management;
import utility.Logging;
import utility.SqlFunction;

import static utility.Utils.*;

import static utility.Logging.logLevel.*;
import static utility.Utils.readProperties;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties serviceProperties = readServicesProperties();
        String[] services = serviceProperties.getProperty("services").split(";");

        Logging logger = new Logging(serviceProperties.getProperty("logLevel"));
        logger.printLog("Inizio processo gestione", DEBUG);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String endDateString = getIeri(sdf);

        for (String servizio : services) {
            processService(servizio, sdf, endDateString, logger, endMessage);
        }

    }

    private static String getIeri(SimpleDateFormat sdf) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return sdf.format(calendar.getTime());
    }

    private static void processService(String servizio, SimpleDateFormat sdf, String endDateString,
                                       Logging logger, List<String> endMessage)
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



        db.closeConnection();
    }
}