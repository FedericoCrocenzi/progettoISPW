package it.ispw.project.dao.dbConnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static DBConnection instance = null; // L'istanza Singleton (Lazy)
    private Connection connection = null;        // L'oggetto Connection gestito dall'istanza
    private final Properties properties = new Properties();
    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());

    /**
     * Costruttore privato.
     * Viene chiamato SOLO quando si invoca getInstance() per la prima volta.
     * Qui spostiamo la logica di caricamento delle configurazioni.
     */
    private DBConnection() {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.log(Level.SEVERE, "Spiacente, impossibile trovare config.properties");
            } else {
                properties.load(input);
            }

            // Caricamento driver
            String driverClass = properties.getProperty("driverClassName");
            if (driverClass != null) {
                Class.forName(driverClass);
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento della configurazione DB", e);
        }
    }

    /**
     * Metodo statico per ottenere l'istanza Singleton (Lazy Initialization).
     * @return l'istanza di DBConnection.
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Restituisce la connessione SQL gestita da questa istanza.
     * Se è chiusa o nulla, la riapre.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String dbUrl = properties.getProperty("dbUrl");
                String user = properties.getProperty("username");
                String pass = properties.getProperty("password");

                connection = DriverManager.getConnection(dbUrl, user, pass);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fallita la connessione al Database", e);
            connection = null; // Reset in caso di errore
        }
        return connection;
    }
}