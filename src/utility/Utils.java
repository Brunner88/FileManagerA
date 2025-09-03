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
            System.exit(1);
        }
        Properties props = new Properties();
        props.load(fis);

        return props;
    }
}
