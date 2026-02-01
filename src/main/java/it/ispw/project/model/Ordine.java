package it.ispw.project.model;


import java.util.Date;
import java.util.Map;

public class Ordine {

    private int id;
    private Date dataCreazione;
    private double totale;
    private String stato; // Es: "PAGATO", "SPEDITO"

    // Relazione con il Cliente (proprietario dell'ordine)
    private Cliente cliente;

    // Composizione: L'ordine contiene la lista degli articoli acquistati
    // Potremmo usare una classe 'RigaOrdine', ma una Map qui è sufficiente per iniziare
    private Map<Articolo, Integer> articoliAcquistati;

    public Ordine(int id, Date dataCreazione, Cliente cliente, Map<Articolo, Integer> articoli, double totale) {
        this.id = id;
        this.dataCreazione = dataCreazione;
        this.cliente = cliente;
        this.articoliAcquistati = articoli;
        this.totale = totale;
        this.stato = "IN_ELABORAZIONE";
    }

    // Metodi di business
    public void completaOrdine() {
        this.stato = "COMPLETATO";
    }

    // Getters & Setters
    public int getId() { return id; }
    public Date getDataCreazione() { return dataCreazione; }
    public double getTotale() { return totale; }
    public String getStato() { return stato; }
    public Cliente getCliente() { return cliente; }
    public Map<Articolo, Integer> getArticoliAcquistati() { return articoliAcquistati; }
}