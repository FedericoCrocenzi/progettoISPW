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

    private final Logger logger = Logger.getLogger(JDBCArticoloDAO.class.getName());

    @Override
    public Articolo selectArticoloById(int id) {
        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return null;

        try (PreparedStatement stmt = conn.prepareStatement(
                Queries.SELECT_ARTICOLO_BY_ID,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return istanziaArticoloDaResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero articolo per ID", e);
        }
        return null;
    }

    @Override
    public List<Articolo> selectAllArticoli() {
        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        List<Articolo> lista = new ArrayList<>();
        if (conn == null) return lista;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Queries.SELECT_ALL_ARTICOLI)) {

            while (rs.next()) {
                Articolo a = istanziaArticoloDaResultSet(rs);
                if (a != null) lista.add(a);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero del catalogo", e);
        }
        return lista;
    }

    @Override
    public List<Articolo> selectByFilter(String testo, String tipo, Double prezzoMin, Double prezzoMax) {
        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        List<Articolo> lista = new ArrayList<>();
        if (conn == null) return lista;

        StringBuilder queryBuilder = new StringBuilder(Queries.SELECT_ARTICOLO_BASE);
        List<Object> params = new ArrayList<>();

        // Costruzione dinamica della query
        if (testo != null && !testo.isEmpty()) {
            queryBuilder.append(" AND descrizione LIKE ?");
            params.add("%" + testo + "%");
        }
        if (tipo != null && !tipo.isEmpty() && !"TUTTI".equalsIgnoreCase(tipo)) {
            queryBuilder.append(" AND tipo = ?");
            params.add(tipo);
        }
        if (prezzoMin != null) {
            queryBuilder.append(" AND prezzo >= ?");
            params.add(prezzoMin);
        }
        if (prezzoMax != null) {
            queryBuilder.append(" AND prezzo <= ?");
            params.add(prezzoMax);
        }

        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            // Impostazione parametri
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Articolo a = istanziaArticoloDaResultSet(rs);
                    if (a != null) lista.add(a);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante la ricerca filtrata", e);
        }
        return lista;
    }

    @Override
    public void updateScorta(Articolo articolo) {
        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) return;

        try (PreparedStatement stmt = conn.prepareStatement(Queries.UPDATE_ARTICOLO_SCORTA)) {
            stmt.setInt(1, articolo.ottieniScorta());
            stmt.setInt(2, articolo.leggiId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore aggiornamento scorta articolo " + articolo.leggiId(), e);
        }
    }

    // Metodo helper privato (rimane invariato)
    private Articolo istanziaArticoloDaResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String desc = rs.getString("descrizione");
        double prezzo = rs.getDouble("prezzo");
        int scorta = rs.getInt("scorta");
        String tipo = rs.getString("tipo");
        String imgPath = rs.getString("immagine_path");

        Articolo articolo = null;

        switch (tipo) {
            case "MANGIME":
                java.sql.Date sqlDate = rs.getDate("data_scadenza");
                java.util.Date utilDate = (sqlDate != null) ? new java.util.Date(sqlDate.getTime()) : null;
                articolo = new Mangime(id, desc, prezzo, scorta, utilDate);
                break;

            case "UTENSILE":
                String materiale = rs.getString("materiale");
                articolo = new Utensile(id, desc, prezzo, scorta, materiale);
                break;

            case "FITOFARMACO":
                boolean patentino = rs.getBoolean("richiede_patentino");
                articolo = new Fitofarmaco(id, desc, prezzo, scorta, patentino);
                break;

            default:
                logger.log(Level.WARNING, "Tipo articolo sconosciuto nel DB: " + tipo);
                return null;
        }

        if (articolo != null) {
            if (imgPath == null || imgPath.trim().isEmpty()) {
                imgPath = "/Image/default.png";
            }
            articolo.setImmaginePath(imgPath);
        }

        return articolo;
    }
}