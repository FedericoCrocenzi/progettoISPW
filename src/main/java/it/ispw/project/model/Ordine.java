package it.ispw.project.model;

import java.util.Date;
import java.util.Map;

public class Ordine {

    private int id;
    private Date dataCreazione;
    private double totale;
    private String stato;

    // CORREZIONE 1: Usiamo Utente, non Cliente (coerenza con Single Table)
    private Utente cliente;

    private Map<Articolo, Integer> articoliAcquistati;

    // Costruttore
    public Ordine(int id, Date dataCreazione, Utente cliente, Map<Articolo, Integer> articoli, double totale) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.cliente = cliente;
        this.articoliAcquistati = articoli;
        this.totale = totale;
        this.stato = "IN_ELABORAZIONE";
    }

    // --- LOGICA DI BUSINESS ---

    public void completaOrdine() {
        this.stato = "COMPLETATO";
    }

    /**
     * CORREZIONE 2: Metodo semantico per la persistenza.
     * Questo NON è un setter generico. Serve per trasformare l'oggetto
     * da "Transiente" (in memoria) a "Persistente" (con ID DB).
     */
    public void registraIdGenerato(int id) {
        if (this.id != 0) {
            // Opzionale: impedisce di sovrascrivere l'ID se esiste già
            throw new IllegalStateException("L'ordine ha già un ID assegnato!");
        }
        this.id = id;
    }

    // --- GETTERS (Solo lettura) ---
    public int leggiId() { return id; }
    public Date getDataCreazione() { return dataCreazione; }
    public double getTotale() { return totale; }
    public String getStato() { return stato; }
    public Utente getCliente() { return cliente; } // Ritorna Utente
    public Map<Articolo, Integer> getArticoliAcquistati() { return articoliAcquistati; }
}