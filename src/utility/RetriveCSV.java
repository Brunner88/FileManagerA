package utility;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import static utility.Logging.logLevel.*;

public class RetriveCSV {

    /**
     * servizio di recupero online del file di rapporto
     * @param url indirizzo del file da recuperare
     * @param report rapporto da recuperare (subscriptio, unsub ecc)
     * @param repository percorso in cui salvare il file recuperato (se non esiste verrà creato)
     * @param date data da recuperare in formato yyyyMMdd
     * @param logger log
     * @return boolean contenete risultato operazione
     */
    public static boolean retrive (String servizio, String url, String report, String repository, String date, Logging logger) {

        String nomeFile = date + "_" + report + ".csv";
        try {

            //controllo se il path al repository esiste, in caso negativo lo creo
            File file = new File(repository + "/" + nomeFile);
            if(!file.getParentFile().exists()){
                if(!file.getParentFile().mkdirs()){
                    logger.printLog("Impossibile creare percorso per salvare il file: " + repository + "/" + nomeFile, ERROR);
                    return false;
                }
            }

            //recupero file
            URL fileUrl = new URL(url);
            InputStream inputStream = fileUrl.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            FileWriter fileWriter = new FileWriter(repository + "/" + nomeFile);
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.write(line + "\n");
            }
            reader.close();
            fileWriter.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.printLog("URL malformata " + url + " durante il tentativo di download del csv " + nomeFile + " per il servizio " + servizio, ERROR);
            return false;
        } catch(FileNotFoundException e){
            logger.printLog("Errore nel recupero del csv " + nomeFile + " per il servizio " + servizio +", il file non è presente nel repository remoto", WARNING);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            logger.printLog("Errore nella lettura dello stream del csv " + nomeFile + " per il servizio " + servizio, ERROR);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            logger.printLog("Errore nel recupero del csv", ERROR);
            return false;
        }
        return true;
    }
}