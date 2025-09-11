package serviceManagement;

import utility.Logging;
import utility.SqlFunction;

public class Management {

    private final SqlFunction connection;

    private final String service;

    private final Logging logger;

    public Management(SqlFunction connection, String service, Logging logger) {
        this.connection = connection;
        this.service = service;
        this.logger = logger;
    }
}
