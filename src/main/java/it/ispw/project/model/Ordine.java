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




    public void setStato(String stato) {
        this.stato = stato;
    }

    public void completaOrdine() {
        this.stato = "COMPLETATO";
    }




    public void registraIdGenerato(int id) {
        if (this.id != 0) {
            // Se l'ID è già settato, evito sovrascritture accidentali

        }
        this.id = id;
    }



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


    public Map<Articolo, Integer> getArticoliAcquistati() {
        return getArticoli();
    }
}
