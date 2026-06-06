package it.ispw.project.dao.demo;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Ordine;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemoryOrdineDAO implements OrdineDAO {

    private static final Logger LOGGER = Logger.getLogger(MemoryOrdineDAO.class.getName());

    // IMPORTANTE: static per simulare la persistenza tra le varie schermate
    private static List<Ordine> tabellaOrdini = new ArrayList<>();

    @Override
    public void insertOrdine(Ordine ordine) {
        // 1. Simulazione Auto-Increment ID
        int nuovoId = 1;
        if (!tabellaOrdini.isEmpty()) {
            // Prendo l'ID dell'ultimo ordine inserito e aggiungo 1
            nuovoId = tabellaOrdini.get(tabellaOrdini.size() - 1).leggiId() + 1;
        }

        // 2. Registro l'ID nel model
        ordine.registraIdGenerato(nuovoId);

        // 3. "Salvo" nella lista condivisa
        tabellaOrdini.add(ordine);

        LOGGER.log(Level.INFO,
                "DEMO (RAM): Ordine salvato con ID {0} | Totale: {1}",
                new Object[]{nuovoId, ordine.getTotale()});
    }

    @Override
    public Ordine selectOrdineById(int id) {
        for (Ordine o : tabellaOrdini) {
            if (o.leggiId() == id) {
                return o;
            }
        }
        return null;
    }

    @Override
    public List<Ordine> findAll() {
        // Restituisco una copia della lista per sicurezza
        return new ArrayList<>(tabellaOrdini);
    }

    @Override
    public List<Ordine> findByStato(String stato) {
        List<Ordine> ordini = new ArrayList<>();
        for (Ordine ordine : tabellaOrdini) {
            if (stato != null && stato.equals(ordine.getStato())) {
                ordini.add(ordine);
            }
        }
        return ordini;
    }

    @Override
    public void updateStato(Ordine ordine) throws DAOException {
        if (ordine == null) {
            throw new DAOException("Ordine non valido durante l'aggiornamento dello stato.");
        }



        for (Ordine o : tabellaOrdini) {
            if (o.leggiId() == ordine.leggiId()) {

                // Qui stampo solo un log di conferma
                LOGGER.log(Level.INFO,
                        "DEMO (RAM): Stato ordine #{0} aggiornato a: {1}",
                        new Object[]{o.leggiId(), ordine.getStato()});
                return;
            }
        }
        throw new DAOException("Ordine non trovato durante l'aggiornamento dello stato.");
    }
}
