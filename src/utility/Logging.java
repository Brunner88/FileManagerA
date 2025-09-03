package utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Logging {

    public enum logLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    private static String LOG_FILE_NAME;
    private static logLevel LOG_LEVEL_SELECTED;

    /**
     * Oggetto per la creazione di log, definisce il nome del file e il livello di profondità dei log
     *
     * @param level String livello di logging (debug, info, warning, error, fatal)
     */
    public Logging(String level) {

        switch (level.toUpperCase()) {
            case "INFO":
                LOG_LEVEL_SELECTED = logLevel.INFO;
                break;
            case "WARNING":
                LOG_LEVEL_SELECTED = logLevel.WARNING;
                break;
            case "ERROR":
                LOG_LEVEL_SELECTED = logLevel.ERROR;
                break;
            case "FATAL":
                LOG_LEVEL_SELECTED = logLevel.FATAL;
                break;
            default:
                LOG_LEVEL_SELECTED = logLevel.DEBUG;
                break;
        }

        Date endDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(endDate);

        LOG_FILE_NAME = "logs/" + date + "_fileManager.log";
    }

    /**
     * Funzione per la scrittura del messaggio nei log e nella console
     *
     * @param text  Stringa contenente il messagio da loggare
     * @param level Enum logLevel che comunica il livello di profondità del singolo log
     */
    public void printLog(String text, logLevel level) {
        switch (LOG_LEVEL_SELECTED) {
            case DEBUG:
                writeLog(text, level);
                break;
            case INFO:
                if (level == logLevel.INFO || level == logLevel.WARNING || level == logLevel.ERROR || level == logLevel.FATAL) {
                    writeLog(text, level);
                }
                break;
            case WARNING:
                if (level == logLevel.WARNING || level == logLevel.ERROR || level == logLevel.FATAL) {
                    writeLog(text, level);
                }
                break;
            case ERROR:
                if (level == logLevel.ERROR || level == logLevel.FATAL) {
                    writeLog(text, level);
                }
                break;
            case FATAL:
                if (level == logLevel.FATAL) {
                    writeLog(text, level);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Funzione che scrive il log nei file e nella console
     *
     * @param text  Stringa contenente il messagio da loggare
     * @param level Enum logLevel che comunica il livello di profondità del singolo log
     */
    private void writeLog(String text, logLevel level) {

        try {
            // Ottengo il timestamp il log e lo formatto
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestampString = now.format(formatter);

            // Credo il filewriter (append true per aggiungere in coda)
            FileWriter fileWriter = new FileWriter(LOG_FILE_NAME, true);

            // BufferedWriter per scrivere il file
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // scrivo
            String toWrite = timestampString + " [" + level + "] " + text;
            bufferedWriter.write(toWrite);
            System.out.println(toWrite);

            // mando a capo per il prossimo inserimento
            bufferedWriter.newLine();

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }

    }
}
