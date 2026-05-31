package it.ispw.project.dao;

import it.ispw.project.dao.demo.DemoDAOFactory;
import it.ispw.project.dao.file_system.FileSystemDAOFactory;
import it.ispw.project.dao.jdbc.JDBCDAOFactory;
import it.ispw.project.config.PersistenceConfig;

/**
 * ABSTRACT FACTORY PATTERN
 * Fornisce un'interfaccia per creare famiglie di oggetti DAO
 * senza specificare le loro classi concrete.
 */
// Singleton intenzionale: mantiene riusabili le factory DAO per la persistenza scelta nel progetto ISPW.
@SuppressWarnings("java:S6548")
public abstract class DAOFactory {

    // Costanti per configurazione
    public static final int JDBC = 1;
    public static final int FILESYSTEM = 2;
    public static final int DEMO = 3;

    // Metodi astratti
    public abstract ArticoloDAO getArticoloDAO();
    public abstract OrdineDAO getOrdineDAO();
    public abstract UtenteDAO getUtenteDAO();

    /**
     * Factory Method statico.
     * Restituisce la factory in base alla configurazione globale
     * impostata all'avvio (CLI).
     */
    public static DAOFactory getDAOFactory() {

        int whichFactory = PersistenceConfig.getPersistenceType();

        switch (whichFactory) {
            case JDBC:
                return JdbcFactoryHolder.INSTANCE;
            case FILESYSTEM:
                return FileSystemFactoryHolder.INSTANCE;
            case DEMO:
                return DemoFactoryHolder.INSTANCE;
            default:
                throw new IllegalStateException(
                        "Tipo di persistenza non valido: " + whichFactory
                );
        }
    }

    private static class JdbcFactoryHolder {
        private static final DAOFactory INSTANCE = new JDBCDAOFactory();
    }

    private static class FileSystemFactoryHolder {
        private static final DAOFactory INSTANCE = new FileSystemDAOFactory();
    }

    private static class DemoFactoryHolder {
        private static final DAOFactory INSTANCE = new DemoDAOFactory();
    }
}
