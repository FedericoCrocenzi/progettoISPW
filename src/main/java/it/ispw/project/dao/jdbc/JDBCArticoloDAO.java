package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries; // Importiamo le costanti SQL
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCArticoloDAO implements ArticoloDAO {

    /**
     * Cerca un articolo per ID (chiave primaria).
     */
    @Override
    public Articolo selectArticoloById(int id) {
        Connection conn = DBConnection.getConnection();

        // Controllo robustezza
        if (conn == null) return null;

        // Uso costante SQL
        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ARTICOLO_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return istanziaArticoloDaResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        if (conn == null) return catalogo; // Ritorna lista vuota per evitare NPE nel controller

        // Uso costante SQL
        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ALL_ARTICOLI);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Articolo art = istanziaArticoloDaResultSet(rs);
                if (art != null) {
                    catalogo.add(art);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

        // Uso costante SQL
        try (PreparedStatement stmt = conn.prepareStatement(Queries.UPDATE_ARTICOLO_SCORTA)) {
            // Usiamo i metodi di lettura del Model
            stmt.setInt(1, articolo.ottieniScorta());
            stmt.setInt(2, articolo.leggiId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
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

        // Costruzione dinamica partendo dalla base "SELECT ... WHERE 1=1"
        StringBuilder queryBuilder = new StringBuilder(Queries.SELECT_ARTICOLO_BASE);

        if (descrizione != null && !descrizione.isEmpty()) {
            queryBuilder.append(" AND descrizione LIKE ?");
        }
        if (tipo != null && !tipo.isEmpty()) {
            queryBuilder.append(" AND tipo = ?");
        }
        if (min != null) {
            queryBuilder.append(" AND prezzo >= ?");
        }
        if (max != null) {
            queryBuilder.append(" AND prezzo <= ?");
        }

        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            int index = 1;

            // Impostazione parametri dinamica
            if (descrizione != null && !descrizione.isEmpty()) {
                stmt.setString(index++, "%" + descrizione + "%");
            }
            if (tipo != null && !tipo.isEmpty()) {
                stmt.setString(index++, tipo);
            }
            if (min != null) {
                stmt.setDouble(index++, min);
            }
            if (max != null) {
                stmt.setDouble(index++, max);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risultati.add(istanziaArticoloDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return risultati;
    }

    // --- METODO PRIVATO DI SUPPORTO (Factory Method Interno) ---
    private Articolo istanziaArticoloDaResultSet(ResultSet rs) throws SQLException {

        // 1. Leggo i dati comuni
        int id = rs.getInt("id");
        String desc = rs.getString("descrizione");
        double prezzo = rs.getDouble("prezzo");
        int scorta = rs.getInt("scorta");
        String tipo = rs.getString("tipo");

        // 2. Switch sul tipo per creare l'istanza specifica
        switch (tipo) {
            case "MANGIME":
                java.sql.Date sqlDate = rs.getDate("data_scadenza");
                java.util.Date utilDate = (sqlDate != null) ? new java.util.Date(sqlDate.getTime()) : null;
                return new Mangime(id, desc, prezzo, scorta, utilDate);

            case "UTENSILE":
                String materiale = rs.getString("materiale");
                return new Utensile(id, desc, prezzo, scorta, materiale);

            case "FITOFARMACO":
                // Handle 0/1 tinyint as boolean
                boolean patentino = rs.getBoolean("richiede_patentino");
                return new Fitofarmaco(id, desc, prezzo, scorta, patentino);

            default:
                System.err.println("Tipo articolo non riconosciuto: " + tipo);
                return null;
        }
    }
}