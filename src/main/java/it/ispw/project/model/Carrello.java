package it.ispw.project.model;

import it.ispw.project.model.observer.Subject;
import java.util.HashMap;
import java.util.Map;

public class Carrello extends Subject {

    // Associazione Articolo -> Quantità
    private Map<Articolo, Integer> contenuto;

    public Carrello() {
        super(); // Inizializza la lista degli observer
        this.contenuto = new HashMap<>();
    }

    public void aggiungiArticolo(Articolo articolo, int quantita) {
        if (articolo == null || quantita <= 0) return;

        // Se l'articolo è già presente, aggiorno la quantità
        if (contenuto.containsKey(articolo)) {
            int qtaAttuale = contenuto.get(articolo);
            contenuto.put(articolo, qtaAttuale + quantita);
        } else {
            // Altrimenti lo aggiungo
            contenuto.put(articolo, quantita);
        }

        // NOTIFICA: Avviso la GUI che il totale è cambiato
        super.notifyObservers(this);
    }

    public void rimuoviArticolo(Articolo articolo) {
        if (contenuto.containsKey(articolo)) {
            contenuto.remove(articolo);
            super.notifyObservers(this);
        }
    }

    public void svuotaCarrello() {
        contenuto.clear();
        super.notifyObservers(this);
    }

    public double calcolaTotale() {
        double totale = 0.0;
        // Itero sulla mappa (EntrySet)
        for (Map.Entry<Articolo, Integer> entry : contenuto.entrySet()) {
            Articolo art = entry.getKey();
            Integer qta = entry.getValue();
            totale += art.ottieniPrezzo() * qta;
        }
        return totale;
    }

    public Map<Articolo, Integer> getContenuto() {
        return contenuto;
    }
}