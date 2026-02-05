package it.ispw.project.dao;

import it.ispw.project.dao.Demo.DemoDAOFactory;
import it.ispw.project.dao.FileSystem.FileSystemDAOFactory;
import it.ispw.project.dao.jdbc.JDBCDAOFactory;

/**
 * ABSTRACT FACTORY PATTERN
 * Fornisce un'interfaccia per creare famiglie di oggetti DAO (ArticoloDAO, OrdineDAO, etc.)
 * senza specificare le loro classi concrete.
 */
public abstract class DAOFactory {

    // Costanti per configurazione
    public static final int JDBC = 1;        // Versione FULL con Database
    public static final int FILESYSTEM = 2;  // Versione FILE
    public static final int DEMO = 3;        // Versione DEMO (In Memoria / Dati finti)

    // Metodi astratti che le factory concrete devono implementare
    public abstract ArticoloDAO getArticoloDAO();
    public abstract OrdineDAO getOrdineDAO();
    public abstract UtenteDAO getUtenteDAO();

    /**
     * Factory Method statico.
     * In base alla configurazione scelta, restituisce la fabbrica corretta.
     */
    public static DAOFactory getDAOFactory(int whichFactory) {
        switch (whichFactory) {
            case JDBC:
                return new JDBCDAOFactory();
            case FILESYSTEM:
                return new FileSystemDAOFactory();
            case DEMO:
                return new DemoDAOFactory();
            default:
                return null;
        }
    }
}