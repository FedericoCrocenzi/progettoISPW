package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.dao.dbConnection.Queries; // Import fondamentale
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;

import java.sql.*;
import java.util.Map;

public class JDBCOrdineDAO implements OrdineDAO {

    @Override
    public void insertOrdine(Ordine ordine) {
        Connection conn = DBConnection.getConnection();

        // 1. Controllo robustezza: se il DB è giù, evitiamo NullPointerException
        if (conn == null) {
            System.err.println("[JDBCOrdineDAO] Errore: Connessione DB assente.");
            return;
        }

        PreparedStatement stmtOrdine = null;
        PreparedStatement stmtRiga = null;

        try {
            // Disabilitiamo l'auto-commit per gestire la TRANSAZIONE
            conn.setAutoCommit(false);

            // =================================================================
            // FASE 1: Inserimento Testata Ordine
            // =================================================================
            // Query: INSERT INTO ordine (data_creazione, totale, stato, id_cliente) VALUES (?, ?, ?, ?)
            stmtOrdine = conn.prepareStatement(Queries.INSERT_ORDINE, Statement.RETURN_GENERATED_KEYS);

            // Conversione data Java -> SQL Timestamp
            stmtOrdine.setTimestamp(1, new Timestamp(ordine.getDataCreazione().getTime()));
            stmtOrdine.setDouble(2, ordine.getTotale());
            stmtOrdine.setString(3, ordine.getStato());
            stmtOrdine.setInt(4, ordine.getCliente().getId());

            stmtOrdine.executeUpdate();

            // =================================================================
            // FASE 2: Recupero ID Generato (Auto-Increment)
            // =================================================================
            try (ResultSet rs = stmtOrdine.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerato = rs.getInt(1);
                    // Aggiorniamo l'oggetto Java con l'ID reale del DB
                    ordine.registraIdGenerato(idGenerato);
                }
            }

            // =================================================================
            // FASE 3: Inserimento Righe Ordine (Batch Insert)
            // =================================================================
            // Query: INSERT INTO riga_ordine (id_ordine, id_articolo, quantita, prezzo_unitario) VALUES (?, ?, ?, ?)
            stmtRiga = conn.prepareStatement(Queries.INSERT_RIGA_ORDINE);

            for (Map.Entry<Articolo, Integer> entry : ordine.getArticoliAcquistati().entrySet()) {
                Articolo art = entry.getKey();
                Integer qta = entry.getValue();

                stmtRiga.setInt(1, ordine.leggiId());   // ID dell'ordine appena generato
                stmtRiga.setInt(2, art.leggiId());      // ID Articolo
                stmtRiga.setInt(3, qta);                // Quantità
                stmtRiga.setDouble(4, art.ottieniPrezzo()); // Prezzo storico

                stmtRiga.addBatch(); // Aggiunge alla coda
            }

            stmtRiga.executeBatch(); // Esegue tutte le insert in un colpo solo

            // =================================================================
            // FASE 4: Commit (Tutto andato bene)
            // =================================================================
            conn.commit();

        } catch (SQLException e) {
            // ROLLBACK: Se qualcosa va storto, annulliamo tutto (niente ordine a metà)
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Chiusura risorse e ripristino AutoCommit
            try {
                if (stmtOrdine != null) stmtOrdine.close();
                if (stmtRiga != null) stmtRiga.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Ordine selectOrdineById(int id) {
        // Implementazione opzionale per l'esame se non richiesta esplicitamente
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        // Query: UPDATE ordine SET stato = ? WHERE id = ?
        try (PreparedStatement stmt = conn.prepareStatement(Queries.UPDATE_ORDINE_STATO)) {
            stmt.setString(1, ordine.getStato());
            stmt.setInt(2, ordine.leggiId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}