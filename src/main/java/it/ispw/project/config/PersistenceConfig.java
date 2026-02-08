package it.ispw.project.config;

import it.ispw.project.dao.DAOFactory;

public class PersistenceConfig {

    private static int persistenceType = DAOFactory.JDBC;

    private PersistenceConfig() {}

    public static void setPersistenceType(int type) {
        persistenceType = type;
    }

    public static int getPersistenceType() {
        return persistenceType;
    }
}