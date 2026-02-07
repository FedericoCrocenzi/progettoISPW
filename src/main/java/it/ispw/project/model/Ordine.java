package it.ispw.project.model;

import java.util.Date;
import java.util.Map;

public class Ordine {

    private int id;
    private Date dataCreazione;
    private double totale;
    private String stato;

    // Riferimento all'Utente (Cliente)
    private Utente cliente;

    // Mappa Articolo -> Quantità
    private Map<Articolo, Integer> articoliAcquistati;

    // Costruttore
    public Ordine(int id, Date dataCreazione, Utente cliente, Map<Articolo, Integer> articoli, double totale) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.cliente = cliente;
        this.articoliAcquistati = articoli;
        this.totale = totale;
        // Stato di default se non specificato
        this.stato = "IN_ELABORAZIONE";
    }

    // --- LOGICA DI BUSINESS E SETTERS ---

    /**
     * Permette di modificare lo stato dell'ordine.
     * Necessario per il Controller Applicativo (es. quando diventa "PRONTO")
     * e per il DAO (quando carica lo stato dal DB).
     */
    public void setStato(String stato) {
        this.stato = stato;
    }

    public void completaOrdine() {
        this.stato = "COMPLETATO";
    }

    /**
     * Metodo per la persistenza: assegna l'ID generato dal DB.
     */
    public void registraIdGenerato(int id) {
        if (this.id != 0) {
            // Se l'ID è già settato, evitiamo sovrascritture accidentali
            // (A meno che non sia una logica voluta, qui lancio eccezione per sicurezza)
            // throw new IllegalStateException("L'ordine ha già un ID assegnato!");
        }
        this.id = id;
    }

    // --- GETTERS ---

    public int leggiId() {
        return id;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public double getTotale() {
        return totale;
    }

    public String getStato() {
        return stato;
    }

    public Utente getCliente() {
        return cliente;
    }

    /**
     * Restituisce la mappa degli articoli.
     * Ho aggiunto questo metodo perché il Controller e il DAO usano spesso "getArticoli()".
     */
    public Map<Articolo, Integer> getArticoli() {
        return articoliAcquistati;
    }

    // Manteniamo anche il getter originale per compatibilità interna se serve
    public Map<Articolo, Integer> getArticoliAcquistati() {
        return articoliAcquistati;
    }
}