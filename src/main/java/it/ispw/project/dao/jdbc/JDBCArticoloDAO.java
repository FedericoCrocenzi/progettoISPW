package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.db_connection.DBConnection;
import it.ispw.project.dao.db_connection.Queries;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.text.MessageFormat;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCArticoloDAO implements ArticoloDAO {

    private static final String DEFAULT_ARTICOLO_IMAGE_PATH = "/image/default.png";

    private final Logger logger = Logger.getLogger(JDBCArticoloDAO.class.getName());
    private final Map<Integer, Articolo> articoliById = new HashMap<>();

    @Override
    public Articolo selectArticoloById(int id) throws DAOException {
        synchronized (articoliById) {
            Articolo articoloInCache = articoliById.get(id);
            if (articoloInCache != null) {
                return articoloInCache;
            }
        }

        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new DAOException("Connessione al database non disponibile.");
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                Queries.SELECT_ARTICOLO_BY_ID,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Articolo articolo = istanziaArticoloDaResultSet(rs);
                    cacheArticolo(articolo);
                    return articolo;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero articolo per ID", e);
            throw new DAOException("Errore durante il recupero dell'articolo.", e);
        }
        return null;
    }

    @Override
    public List<Articolo> selectAllArticoli() throws DAOException {
        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        List<Articolo> lista = new ArrayList<>();
        if (conn == null) {
            throw new DAOException("Connessione al database non disponibile.");
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Queries.SELECT_ALL_ARTICOLI)) {

            while (rs.next()) {
                Articolo a = istanziaArticoloDaResultSet(rs);
                if (a != null) {
                    cacheArticolo(a);
                    lista.add(a);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante il recupero del catalogo", e);
            throw new DAOException("Errore durante il recupero del catalogo.", e);
        }
        return lista;
    }

    @Override
    public List<Articolo> selectByDescrizione(String testo) throws DAOException {
        if (testo == null || testo.isEmpty()) {
            return selectAllArticoli();
        }

        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        List<Articolo> lista = new ArrayList<>();
        if (conn == null) {
            throw new DAOException("Connessione al database non disponibile.");
        }

        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ARTICOLO_BY_DESCRIZIONE)) {
            stmt.setString(1, "%" + testo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Articolo a = istanziaArticoloDaResultSet(rs);
                    if (a != null) {
                        cacheArticolo(a);
                        lista.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante la ricerca filtrata", e);
            throw new DAOException("Errore durante la ricerca degli articoli.", e);
        }
        return lista;
    }

    @Override
    public boolean updateScorta(Articolo articolo) throws DAOException {
        if (articolo == null) {
            return false;
        }

        // MODIFICA QUI: Uso del Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new DAOException("Connessione al database non disponibile.");
        }

        try (PreparedStatement stmt = conn.prepareStatement(Queries.UPDATE_ARTICOLO_SCORTA)) {
            stmt.setInt(1, articolo.ottieniScorta());
            stmt.setInt(2, articolo.leggiId());

            int righeAggiornate = stmt.executeUpdate();
            if (righeAggiornate > 0) {
                cacheArticolo(articolo);
                return true;
            } else {
                invalidaArticolo(articolo.leggiId());
                return false;
            }
        } catch (SQLException e) {
            invalidaArticolo(articolo.leggiId());
            logger.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Errore aggiornamento scorta articolo {0}", articolo.leggiId()));
            throw new DAOException("Errore durante l'aggiornamento della scorta.", e);
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
                logger.log(Level.WARNING, "Tipo articolo sconosciuto nel DB: {0}", tipo);
                throw new SQLException("Tipo articolo sconosciuto nel DB: " + tipo);
        }

        if (articolo != null) {
            if (imgPath == null || imgPath.trim().isEmpty()) {
                imgPath = DEFAULT_ARTICOLO_IMAGE_PATH;
            }
            articolo.setImmaginePath(imgPath);
        }

        return articolo;
    }

    private void cacheArticolo(Articolo articolo) {
        if (articolo != null) {
            synchronized (articoliById) {
                articoliById.put(articolo.leggiId(), articolo);
            }
        }
    }

    private void invalidaArticolo(int id) {
        synchronized (articoliById) {
            articoliById.remove(id);
        }
    }
}
