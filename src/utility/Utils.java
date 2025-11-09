package utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Utils {

    /**
     * legge le configurazioni generali
     *
     * @return Properties contenente le proprietà caricate
     * @throws IOException
     */
    public static Properties readServicesProperties() throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("config/generalConfig.properties");
        } catch (FileNotFoundException e) {
            System.out.println("File non trovato");
            System.exit(1);
        }
        Properties props = new Properties();
        props.load(fis);

        return props;
    }

    /**
     * legge le configurazioni specifiche di un servizio
     *
     * @param service servizio da
     * @return Properties contenente le proprietà caricate
     * @throws IOException
     */
    public static Properties readProperties(String service) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("config/" + service + "_config.properties");
        } catch (FileNotFoundException e) {
            System.out.println("File non trovato");
            System.exit(1);
        }
        Properties props = new Properties();
        props.load(fis);

        return props;
    }

    /**
     * Aumenta o diminuisce una data del valore k di giorni
     *
     * @param currentDate data da modificare
     * @param k           numero di giorni da aggiungere o sottrarre
     * @return
     */
    public static Date alterDate(Date currentDate, int k) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, k);

        return calendar.getTime();
    }
}
