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

    private static Connection connection;
    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private static final Properties properties = new Properties();

    // 1. Caricamento statico delle configurazioni (avviene una sola volta all'avvio)
    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.log(Level.SEVERE, "Spiacente, impossibile trovare config.properties");
            } else {
                properties.load(input);
            }

            // Caricamento driver (opzionale ma consigliato per compatibilità)
            String driverClass = properties.getProperty("driverClassName");
            if (driverClass != null) Class.forName(driverClass);

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento della configurazione DB", e);
        }
    }

    private DBConnection() {}

    /**
     * Restituisce l'istanza singleton della connessione.
     * Se la connessione è chiusa o nulla, prova a riaprirla.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String dbUrl = properties.getProperty("dbUrl");
                String user = properties.getProperty("username");
                String pass = properties.getProperty("password");

                connection = DriverManager.getConnection(dbUrl, user, pass);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fallita la connessione al Database", e);
            // Opzionale: return null o rilanciare un'eccezione custom
            connection = null;
        }
        return connection;
    }
}