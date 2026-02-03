package it.ispw.project.dao.dbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/agricola_db";
    private static final String USER = "root"; // Metti il tuo user (es. root)
    private static final String PASS = "root"; // Metti la tua password (o lascia vuoto "")
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static Connection conn = null;

    private DBConnection() {}

    public static Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName(DRIVER);
                conn = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                // In un progetto reale qui lanceremmo una SystemException
            }
        }
        return conn;
    }
}