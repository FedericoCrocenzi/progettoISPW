package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;

import java.sql.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCOrdineDAO implements OrdineDAO {

    private final Logger logger = Logger.getLogger(JDBCOrdineDAO.class.getName());

    @Override
    public void insertOrdine(Ordine ordine) {
        Connection conn = DBConnection.getConnection();

        // 1. Controllo robustezza
        if (conn == null) {
            logger.log(Level.WARNING, "[JDBCOrdineDAO] Impossibile effettuare insert: Connessione assente.");
            return;
        }

        PreparedStatement stmtOrdine = null;
        PreparedStatement stmtRiga = null;

        try {
            // Disabilitiamo l'auto-commit per gestire la TRANSAZIONE (Testata + Righe)
            conn.setAutoCommit(false);

            // =================================================================
            // FASE 1: Inserimento Testata Ordine
            // =================================================================
            // Nota: Qui usiamo RETURN_GENERATED_KEYS perché è fondamentale recuperare l'ID
            stmtOrdine = conn.prepareStatement(Queries.INSERT_ORDINE, Statement.RETURN_GENERATED_KEYS);

            // Conversione data Java -> SQL Timestamp
            stmtOrdine.setTimestamp(1, new Timestamp(ordine.getDataCreazione().getTime()));
            stmtOrdine.setDouble(2, ordine.getTotale());
            stmtOrdine.setString(3, ordine.getStato());
            stmtOrdine.setInt(4, ordine.getCliente().ottieniId());

            int affectedRows = stmtOrdine.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creazione ordine fallita, nessuna riga inserita.");
            }

            // =================================================================
            // FASE 2: Recupero ID Generato (Auto-Increment)
            // =================================================================
            try (ResultSet rs = stmtOrdine.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerato = rs.getInt(1);
                    ordine.registraIdGenerato(idGenerato);
                } else {
                    throw new SQLException("Creazione ordine fallita, nessun ID ottenuto.");
                }
            }

            // =================================================================
            // FASE 3: Inserimento Righe Ordine (Batch Insert)
            // =================================================================
            stmtRiga = conn.prepareStatement(Queries.INSERT_RIGA_ORDINE);

            for (Map.Entry<Articolo, Integer> entry : ordine.getArticoliAcquistati().entrySet()) {
                Articolo art = entry.getKey();
                Integer qta = entry.getValue();

                stmtRiga.setInt(1, ordine.leggiId());   // ID appena generato
                stmtRiga.setInt(2, art.leggiId());
                stmtRiga.setInt(3, qta);
                stmtRiga.setDouble(4, art.ottieniPrezzo());

                stmtRiga.addBatch();
            }

            stmtRiga.executeBatch();

            // =================================================================
            // FASE 4: Commit (Conferma Transazione)
            // =================================================================
            conn.commit();
            logger.log(Level.INFO, "Ordine creato con successo. ID: " + ordine.leggiId());

        } catch (SQLException e) {
            // ROLLBACK: Se qualcosa va storto, annulliamo tutto
            logger.log(Level.SEVERE, "Errore durante la transazione ordine. Eseguo Rollback.", e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Errore durante il rollback", ex);
            }
        } finally {
            // Chiusura risorse e ripristino AutoCommit
            try {
                if (stmtOrdine != null) stmtOrdine.close();
                if (stmtRiga != null) stmtRiga.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Errore nella chiusura delle risorse", e);
            }
        }
    }

    @Override
    public Ordine selectOrdineById(int id) {
        // Implementazione futura se necessaria
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try (
                // Qui usiamo lo stile WanderWise con i parametri ResultSet
                PreparedStatement stmt = conn.prepareStatement(
                        Queries.UPDATE_ORDINE_STATO,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY)
        ) {
            stmt.setString(1, ordine.getStato());
            stmt.setInt(2, ordine.leggiId());

            int result = stmt.executeUpdate();

            if (result > 0) {
                logger.log(Level.INFO, "Stato ordine aggiornato. ID: " + ordine.leggiId() + " -> " + ordine.getStato());
            } else {
                logger.log(Level.WARNING, "Nessun ordine trovato per l'aggiornamento stato. ID: " + ordine.leggiId());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante l'aggiornamento dello stato ordine", e);
        }
    }
}