package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCArticoloDAO implements ArticoloDAO {

    // Logger per tracciare operazioni ed errori in modo professionale
    private final Logger logger = Logger.getLogger(JDBCArticoloDAO.class.getName());

    /**
     * Cerca un articolo per ID (chiave primaria).
     */
    @Override
    public Articolo selectArticoloById(int id) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;

        try (
                // Configurazione dello statement come nell'esempio WanderWise
                PreparedStatement stmt = conn.prepareStatement(
                        Queries.SELECT_ARTICOLO_BY_ID,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // rs.next() è più sicuro di rs.first() per set vuoti
                    return istanziaArticoloDaResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero dell'articolo per ID: " + id, e);
        }
        return null;
    }

    /**
     * Restituisce TUTTO il catalogo.
     */
    @Override
    public List<Articolo> selectAllArticoli() {
        List<Articolo> catalogo = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return catalogo;

        try (
                PreparedStatement stmt = conn.prepareStatement(
                        Queries.SELECT_ALL_ARTICOLI,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Articolo art = istanziaArticoloDaResultSet(rs);
                if (art != null) {
                    catalogo.add(art);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero del catalogo completo", e);
        }
        return catalogo;
    }

    /**
     * Aggiorna SOLO la quantità disponibile (Scorta).
     */
    @Override
    public void updateScorta(Articolo articolo) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try (
                PreparedStatement stmt = conn.prepareStatement(
                        Queries.UPDATE_ARTICOLO_SCORTA,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setInt(1, articolo.ottieniScorta());
            stmt.setInt(2, articolo.leggiId());

            int result = stmt.executeUpdate();

            // Logghiamo il successo come nell'esempio
            if (result > 0) {
                logger.log(Level.INFO, "Scorta aggiornata per Articolo ID: " + articolo.leggiId());
            } else {
                logger.log(Level.WARNING, "Nessun articolo aggiornato. ID potrebbe non esistere: " + articolo.leggiId());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore aggiornamento scorta articolo ID: " + articolo.leggiId(), e);
        }
    }

    /**
     * Ricerca avanzata per filtri (Nome, Tipo, Prezzo).
     */
    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        List<Articolo> risultati = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return risultati;

        // Costruzione query dinamica
        StringBuilder queryBuilder = new StringBuilder(Queries.SELECT_ARTICOLO_BASE);

        if (descrizione != null && !descrizione.isEmpty()) queryBuilder.append(" AND descrizione LIKE ?");
        if (tipo != null && !tipo.isEmpty()) queryBuilder.append(" AND tipo = ?");
        if (min != null) queryBuilder.append(" AND prezzo >= ?");
        if (max != null) queryBuilder.append(" AND prezzo <= ?");

        try (
                PreparedStatement stmt = conn.prepareStatement(
                        queryBuilder.toString(),
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY)
        ) {
            int index = 1;

            if (descrizione != null && !descrizione.isEmpty()) stmt.setString(index++, "%" + descrizione + "%");
            if (tipo != null && !tipo.isEmpty()) stmt.setString(index++, tipo);
            if (min != null) stmt.setDouble(index++, min);
            if (max != null) stmt.setDouble(index++, max);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risultati.add(istanziaArticoloDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nella ricerca filtrata articoli", e);
        }
        return risultati;
    }

    // --- METODO PRIVATO DI SUPPORTO ---
    private Articolo istanziaArticoloDaResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String desc = rs.getString("descrizione");
        double prezzo = rs.getDouble("prezzo");
        int scorta = rs.getInt("scorta");
        String tipo = rs.getString("tipo");

        switch (tipo) {
            case "MANGIME":
                java.sql.Date sqlDate = rs.getDate("data_scadenza");
                java.util.Date utilDate = (sqlDate != null) ? new java.util.Date(sqlDate.getTime()) : null;
                return new Mangime(id, desc, prezzo, scorta, utilDate);

            case "UTENSILE":
                String materiale = rs.getString("materiale");
                return new Utensile(id, desc, prezzo, scorta, materiale);

            case "FITOFARMACO":
                boolean patentino = rs.getBoolean("richiede_patentino");
                return new Fitofarmaco(id, desc, prezzo, scorta, patentino);

            default:
                logger.log(Level.WARNING, "Tipo articolo sconosciuto nel DB: " + tipo);
                return null;
        }
    }
}