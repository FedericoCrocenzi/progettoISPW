package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtenteDAO implements UtenteDAO {

    @Override
    public Utente checkCredentials(String username, String password) {
        Connection conn = DBConnection.getConnection();
        String query = "SELECT * FROM utente WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // MODIFICA: Uso il costruttore completo invece dei setter!
                    return new Utente(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("ruolo"),
                            rs.getString("email"),     // Se è null nel DB, passa null. Corretto.
                            rs.getString("indirizzo")  // Se è null nel DB, passa null. Corretto.
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}