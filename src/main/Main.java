package main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import utility.Logging;
import static utility.Utils.*;

import static utility.Logging.logLevel.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties serviceProperties = readServicesProperties();
        String[] services = serviceProperties.getProperty("services").split(";");

        System.out.println(services[0]);
        System.out.println(services[1]);
        System.out.println(services[2]);

        Logging logger = new Logging(serviceProperties.getProperty("logLevel"));
        logger.printLog("Inizio processo gestione", DEBUG);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String endDateString = sdf.format(calendar.getTime());

        System.out.println(endDateString);
    }
}