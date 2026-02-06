package it.ispw.project.dao.dbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // 1. CREDENZIALI DEL DATABASE (L'utente tecnico creato via SQL)
    // NON toccare questi valori: servono per "accendere" la connessione.
    private static final String USER = "ispw_user";
    private static final String PASS = "password123";

    // Assicurati che il nome del DB dopo la porta 3306 sia corretto
    private static final String DB_URL = "jdbc:mysql://localhost:3306/applicazioneISPW";
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    private DBConnection() {}

    /**
     * Restituisce la connessione attiva al Database.
     */
    public static Connection getConnection() {
        try {
            // Caricamento del Driver (necessario per versioni vecchie di Java/Tomcat, male non fa)
            Class.forName(DRIVER_CLASS_NAME);

            // Creazione connessione usando l'Utente Tecnico
            return DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("ERRORE CRITICO DB CONNECTION:");
            e.printStackTrace();
            return null;
        }
    }
}