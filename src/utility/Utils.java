package utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
}
