package it.ispw.project.dao.Demo;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.model.Ordine;

import java.util.ArrayList;
import java.util.List;

public class MemoryOrdineDAO implements OrdineDAO {

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

        System.out.println("DEMO (RAM): Ordine salvato con ID " + nuovoId + " | Totale: " + ordine.getTotale());
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
    public void updateStato(Ordine ordine) {
        // In memoria (passaggio per riferimento), l'oggetto 'ordine' passato
        // è lo stesso che sta nella lista 'tabellaOrdini'.
        // Quindi l'aggiornamento è automatico.
        // Tuttavia, per simulare un comportamento realistico di update:

        for (Ordine o : tabellaOrdini) {
            if (o.leggiId() == ordine.leggiId()) {
                // In un DB vero faresti: UPDATE ... SET stato = ...
                // Qui stampiamo solo un log di conferma
                System.out.println("DEMO (RAM): Stato ordine #" + o.leggiId() + " aggiornato a: " + ordine.getStato());
                return;
            }
        }
    }
}