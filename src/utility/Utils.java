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

    /**
     * Controlla se l'array di stringhe passato contiene elementi null
     *
     * @param arrayToCheck array da controllare
     * @return true se contiene almeno un elemento null, false altrimenti
     */
    public static boolean containsNull(String[] arrayToCheck) {
        for (String element : arrayToCheck) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla se un prodotto è contenuta in un array di Product
     *
     * @param element      stringa da cercare
     * @param arrayToCheck array di Product da controllare
     * @return true se viene trovata, false altrimenti
     */
    public static boolean isInProductsArray(String element, Product[] arrayToCheck) {
        for (Product target : arrayToCheck) {
            if (element.equals(target.getProductName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla se una stringa è contenuta in un array di stringhe
     *
     * @param element      stringa da cercare
     * @param arrayToCheck array da controllare
     * @return true se viene trovata, false altrimenti
     */
    public static boolean isInArray(String element, String[] arrayToCheck) {
        for (String target : arrayToCheck) {
            if (element.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
