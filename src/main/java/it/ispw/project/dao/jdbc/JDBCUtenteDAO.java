package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries; // Importiamo la classe con le costanti SQL
import it.ispw.project.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtenteDAO implements UtenteDAO {

    /**
     * Verifica le credenziali nel DB.
     * @param identifier Può essere sia lo username (es. "mario.rossi") che l'email (es. "mario@gmail.com")
     * @param password La password in chiaro
     * @return L'oggetto Utente se trovato, altrimenti null.
     */
    @Override
    public Utente checkCredentials(String identifier, String password) {
        Connection conn = DBConnection.getConnection();

        // 1. Controllo di sicurezza: se la connessione è fallita, ritorniamo null invece di crashare
        if (conn == null) {
            System.err.println("[JDBCUtenteDAO] Errore: Connessione al database non disponibile.");
            return null;
        }

        // 2. Uso la query centralizzata
        // Query attesa: SELECT * FROM utente WHERE (username = ? OR email = ?) AND password = ?
        String query = Queries.SELECT_UTENTE_BY_CREDS;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            // 3. Binding dei parametri
            // Il primo ? è per username, il secondo ? è per email.
            // Passiamo lo stesso input ("identifier") a entrambi.
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 4. Costruzione dell'oggetto Utente con tutti i dati recuperati
                    return new Utente(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("ruolo"),
                            rs.getString("email"),
                            rs.getString("indirizzo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}