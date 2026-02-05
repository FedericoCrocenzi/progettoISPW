package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.dbConnection.DBConnection;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;

import java.sql.*;
import java.util.Map;

public class JDBCOrdineDAO implements OrdineDAO {

    @Override
    public void insertOrdine(Ordine ordine) {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmtOrdine = null;
        PreparedStatement stmtRiga = null;

        try {
            conn.setAutoCommit(false); // Inizio Transazione

            // 1. Insert Testata Ordine
            String queryOrdine = "INSERT INTO ordine (totale, stato, id_cliente) VALUES (?, ?, ?)";
            stmtOrdine = conn.prepareStatement(queryOrdine, Statement.RETURN_GENERATED_KEYS);

            stmtOrdine.setDouble(1, ordine.getTotale());
            stmtOrdine.setString(2, ordine.getStato());
            // Nota: Qui usiamo ordine.getCliente() che ora restituisce un Utente
            stmtOrdine.setInt(3, ordine.getCliente().getId());

            stmtOrdine.executeUpdate();

            // 2. Recupero ID Generato e aggiorno il Model
            try (ResultSet rs = stmtOrdine.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerato = rs.getInt(1);

                    // --- MODIFICA QUI ---
                    // Usiamo il metodo semantico, non un setter
                    ordine.registraIdGenerato(idGenerato);
                }
            }

            // 3. Insert Righe Ordine
            String queryRiga = "INSERT INTO riga_ordine (id_ordine, id_articolo, quantita, prezzo_unitario) VALUES (?, ?, ?, ?)";
            stmtRiga = conn.prepareStatement(queryRiga);

            for (Map.Entry<Articolo, Integer> entry : ordine.getArticoliAcquistati().entrySet()) {
                Articolo art = entry.getKey();
                Integer qta = entry.getValue();

                stmtRiga.setInt(1, ordine.leggiId()); // Uso il getter di lettura
                stmtRiga.setInt(2, art.leggiId());
                stmtRiga.setInt(3, qta);
                stmtRiga.setDouble(4, art.ottieniPrezzo());

                stmtRiga.addBatch();
            }

            stmtRiga.executeBatch();
            conn.commit(); // Conferma Transazione

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Annulla tutto in caso di errore
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Chiusura risorse
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (stmtOrdine != null) stmtOrdine.close();
                if (stmtRiga != null) stmtRiga.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ... metodi select e update rimangono uguali ...
    @Override
    public Ordine selectOrdineById(int id) { return null; }

    @Override
    public void updateStato(Ordine ordine) {
        // Implementazione update
    }
}