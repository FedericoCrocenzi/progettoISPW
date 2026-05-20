package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCOrdineDAO implements OrdineDAO {

    private final Logger logger = Logger.getLogger(JDBCOrdineDAO.class.getName());
    private final UtenteDAO utenteDAO;
    private final ArticoloDAO articoloDAO;

    public JDBCOrdineDAO() {
        this(JDBCDAOFactory.getUtenteDAOCondiviso(), JDBCDAOFactory.getArticoloDAOCondiviso());
    }

    JDBCOrdineDAO(UtenteDAO utenteDAO, ArticoloDAO articoloDAO) {
        this.utenteDAO = utenteDAO;
        this.articoloDAO = articoloDAO;
    }

    @Override
    public void insertOrdine(Ordine ordine) throws DAOException {
        // MODIFICA QUI: Accesso tramite Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn == null) {
            throw new DAOException("Connessione al database non disponibile.");
        }

        PreparedStatement stmtOrdine = null;
        PreparedStatement stmtRiga = null;

        try {
            conn.setAutoCommit(false); // Inizio Transazione

            // 1. Insert Testata Ordine
            stmtOrdine = conn.prepareStatement(Queries.INSERT_ORDINE, Statement.RETURN_GENERATED_KEYS);

            stmtOrdine.setTimestamp(1, new Timestamp(ordine.getDataCreazione().getTime()));
            stmtOrdine.setDouble(2, ordine.getTotale());
            stmtOrdine.setString(3, ordine.getStato());
            stmtOrdine.setInt(4, ordine.getCliente().ottieniId());

            int affectedRows = stmtOrdine.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creazione ordine fallita.");

            // 2. Recupero ID
            try (ResultSet rs = stmtOrdine.getGeneratedKeys()) {
                if (rs.next()) {
                    ordine.registraIdGenerato(rs.getInt(1));
                } else {
                    throw new SQLException("Nessun ID ottenuto.");
                }
            }

            // 3. Insert Righe (Batch)
            stmtRiga = conn.prepareStatement(Queries.INSERT_RIGA_ORDINE);

            for (Map.Entry<Articolo, Integer> entry : ordine.getArticoli().entrySet()) {
                Articolo art = entry.getKey();
                int qta = entry.getValue();

                stmtRiga.setInt(1, ordine.leggiId());
                stmtRiga.setInt(2, art.leggiId());
                stmtRiga.setInt(3, qta);
                stmtRiga.setDouble(4, art.ottieniPrezzo());

                stmtRiga.addBatch();
            }
            stmtRiga.executeBatch();

            conn.commit(); // Conferma Transazione

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Rollback inserimento ordine", e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Rollback ordine non riuscito", ex);
            }
            throw new DAOException("Errore durante il salvataggio dell'ordine.", e);
        } finally {
            try {
                if (stmtOrdine != null) stmtOrdine.close();
                if (stmtRiga != null) stmtRiga.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Errore chiusura risorse ordine", e);
            }
        }
    }

    @Override
    public Ordine selectOrdineById(int id) throws DAOException {
        // MODIFICA QUI: Accesso tramite Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        Ordine ordine = null;

        if (conn == null) throw new DAOException("Connessione al database non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ORDINE_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ordine = mapRowToOrdine(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore selectOrdineById: " + id, e);
            throw new DAOException("Errore durante il recupero dell'ordine.", e);
        }
        return ordine;
    }

    @Override
    public List<Ordine> findAll() throws DAOException {
        // MODIFICA QUI: Accesso tramite Singleton
        Connection conn = DBConnection.getInstance().getConnection();
        List<Ordine> lista = new ArrayList<>();

        if (conn == null) throw new DAOException("Connessione al database non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ALL_ORDINI);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ordine o = mapRowToOrdine(rs);
                if (o != null) lista.add(o);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore findAll ordini", e);
            throw new DAOException("Errore durante il recupero degli ordini.", e);
        }
        return lista;
    }

    @Override
    public List<Ordine> findByStato(String stato) throws DAOException {
        Connection conn = DBConnection.getInstance().getConnection();
        List<Ordine> lista = new ArrayList<>();

        if (conn == null) throw new DAOException("Connessione al database non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_ORDINI_BY_STATO)) {
            stmt.setString(1, stato);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ordine o = mapRowToOrdine(rs);
                    if (o != null) lista.add(o);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore findByStato ordini", e);
            throw new DAOException("Errore durante il recupero degli ordini per stato.", e);
        }
        return lista;
    }

    @Override
    public void updateStato(Ordine ordine) throws DAOException {
        // MODIFICA QUI: Accesso tramite Singleton
        Connection conn = DBConnection.getInstance().getConnection();

        if (conn == null) throw new DAOException("Connessione al database non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(Queries.UPDATE_ORDINE_STATO)) {
            stmt.setString(1, ordine.getStato());
            stmt.setInt(2, ordine.leggiId());
            int righe = stmt.executeUpdate();
            if (righe == 0) {
                throw new DAOException("Ordine non trovato durante l'aggiornamento dello stato.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore updateStato", e);
            throw new DAOException("Errore durante l'aggiornamento dello stato ordine.", e);
        }
    }

    // =================================================================
    // HELPER METHODS (Private)
    // =================================================================

    private Ordine mapRowToOrdine(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Timestamp data = rs.getTimestamp("data_creazione");
        double totale = rs.getDouble("totale");
        String stato = rs.getString("stato");
        int idCliente = rs.getInt("id_cliente");

        // 1. Recupero Utente
        Utente cliente = null;
        try {
            cliente = utenteDAO.findById(idCliente);
        } catch (Exception e) {
            throw new SQLException("Errore recupero cliente per ordine " + id, e);
        }

        // 2. Recupero Articoli
        Map<Articolo, Integer> articoli = getRigheOrdine(id);

        Ordine o = new Ordine(id, new Date(data.getTime()), cliente, articoli, totale);
        o.setStato(stato);
        return o;
    }

    private Map<Articolo, Integer> getRigheOrdine(int idOrdine) {
        Map<Articolo, Integer> mappa = new HashMap<>();
        // MODIFICA QUI: Accesso tramite Singleton
        Connection conn = DBConnection.getInstance().getConnection();

        if (conn == null) return mappa;

        try (PreparedStatement stmt = conn.prepareStatement(Queries.SELECT_RIGHE_BY_ORDINE)) {
            stmt.setInt(1, idOrdine);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idArticolo = rs.getInt("id_articolo");
                    int qta = rs.getInt("quantita");

                    try {
                        Articolo a = articoloDAO.selectArticoloById(idArticolo);
                        if (a != null) {
                            mappa.put(a, qta);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Errore nel recupero articolo id=" + idArticolo, e);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Errore SQL recupero righe ordine " + idOrdine, e);
        }
        return mappa;
    }
}
