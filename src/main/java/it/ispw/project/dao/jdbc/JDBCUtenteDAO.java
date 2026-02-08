package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtenteDAO implements UtenteDAO {

    @Override
    public Utente checkCredentials(String identifier, String password) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // MODIFICA QUI: Accesso tramite Singleton
            conn = DBConnection.getInstance().getConnection();

            if (conn == null) {
                throw new DAOException("Impossibile connettersi al database: Connessione null.");
            }

            stmt = conn.prepareStatement(
                    Queries.SELECT_UTENTE_BY_CREDS,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, password);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToUtente(rs);
            }

            return null; // Utente non trovato

        } catch (SQLException e) {
            throw new DAOException("Errore durante il login per l'utente: " + identifier, e);
        } finally {
            closeResources(rs, stmt);
        }
    }

    @Override
    public Utente findById(int id) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // MODIFICA QUI: Accesso tramite Singleton
            conn = DBConnection.getInstance().getConnection();

            if (conn == null) {
                throw new DAOException("Impossibile connettersi al database: Connessione null.");
            }

            stmt = conn.prepareStatement(
                    Queries.SELECT_UTENTE_BY_ID,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );

            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToUtente(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dell'utente con ID: " + id, e);
        } finally {
            closeResources(rs, stmt);
        }
    }

    @Override
    public void salva(Utente utente) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // MODIFICA QUI: Accesso tramite Singleton
            conn = DBConnection.getInstance().getConnection();

            if (conn == null) {
                throw new DAOException("Impossibile connettersi al database: Connessione null.");
            }

            stmt = conn.prepareStatement(Queries.INSERT_UTENTE);

            stmt.setString(1, utente.leggiUsername());
            stmt.setString(2, utente.ottieniPassword());
            stmt.setString(3, utente.scopriRuolo());
            stmt.setString(4, utente.leggiEmail());
            stmt.setString(5, utente.leggiIndirizzo());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creazione utente fallita, nessuna riga aggiunta.");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il salvataggio dell'utente: " + utente.leggiUsername(), e);
        } finally {
            closeResources(null, stmt);
        }
    }

    // --- Helper Methods ---

    private Utente mapRowToUtente(ResultSet rs) throws SQLException {
        return new Utente(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("ruolo"),
                rs.getString("email"),
                rs.getString("indirizzo")
        );
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            // Non chiudiamo la connessione qui perché è gestita dal Singleton condiviso
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}