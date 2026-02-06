package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries;
import it.ispw.project.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCUtenteDAO implements UtenteDAO {

    // Logger per tracciare operazioni ed errori in modo professionale
    private final Logger logger = Logger.getLogger(JDBCUtenteDAO.class.getName());

    /**
     * Verifica le credenziali nel DB.
     * @param identifier Può essere sia lo username (es. "mario.rossi") che l'email (es. "mario@gmail.com")
     * @param password La password in chiaro
     * @return L'oggetto Utente se trovato, altrimenti null.
     */
    @Override
    public Utente checkCredentials(String identifier, String password) {
        Connection conn = DBConnection.getConnection();

        // 1. Controllo di sicurezza con Logger
        if (conn == null) {
            logger.log(Level.WARNING, "[JDBCUtenteDAO] Errore: Connessione al database non disponibile.");
            return null;
        }

        try (
                // 2. Configurazione dello statement stile WanderWise
                PreparedStatement stmt = conn.prepareStatement(
                        Queries.SELECT_UTENTE_BY_CREDS,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY)
        ) {
            // 3. Binding dei parametri
            // Il primo ? è per username, il secondo ? è per email.
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, password);

            try (ResultSet rs = stmt.executeQuery()) {
                // Usiamo rs.next() che è standard per verificare se c'è almeno un risultato
                if (rs.next()) {

                    // Log opzionale per debug (utile per capire chi si è loggato)
                    logger.log(Level.INFO, "Login effettuato con successo per: " + identifier);

                    // 4. Costruzione dell'oggetto Utente
                    return new Utente(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("ruolo"),
                            rs.getString("email"),
                            rs.getString("indirizzo")
                    );
                } else {
                    logger.log(Level.INFO, "Tentativo di login fallito (credenziali errate) per: " + identifier);
                }
            }
        } catch (SQLException e) {
            // Gestione errore professionale
            logger.log(Level.SEVERE, "Errore SQL durante il controllo credenziali per: " + identifier, e);
        }

        return null;
    }
}