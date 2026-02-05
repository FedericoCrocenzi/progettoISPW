package it.ispw.project.model;

import it.ispw.project.model.observer.Subject;
import java.util.HashMap;
import java.util.Map;

public class Carrello extends Subject {

    // Associazione Articolo -> Quantità
    private Map<Articolo, Integer> contenuto;

    public Carrello() {
        super();
        this.contenuto = new HashMap<>();
    }

    public void aggiungiArticolo(Articolo articolo, int quantita) {
        if (articolo == null || quantita <= 0) return;

        if (contenuto.containsKey(articolo)) {
            int qtaAttuale = contenuto.get(articolo);
            contenuto.put(articolo, qtaAttuale + quantita);
        } else {
            contenuto.put(articolo, quantita);
        }

        super.notifyObservers(this);
    }

    public void rimuoviArticolo(Articolo articolo) {
        if (contenuto.containsKey(articolo)) {
            contenuto.remove(articolo);
            super.notifyObservers(this);
        }
    }

    // --- CORREZIONE 1: Rinominato da svuotaCarrello() a svuota() ---
    // Questo combacia con la chiamata "this.carrello.svuota()" del Controller
    public void svuota() {
        contenuto.clear();
        super.notifyObservers(this);
    }

    public double calcolaTotale() {
        double totale = 0.0;
        for (Map.Entry<Articolo, Integer> entry : contenuto.entrySet()) {
            Articolo art = entry.getKey();
            Integer qta = entry.getValue();
            totale += art.ottieniPrezzo() * qta;
        }
        return totale;
    }

    // --- CORREZIONE 2: Rinominato da getContenuto() a getListaArticoli() ---
    // Questo risolve l'errore "Cannot resolve method getListaArticoli"
    public Map<Articolo, Integer> getListaArticoli() {
        return contenuto;
    }
}