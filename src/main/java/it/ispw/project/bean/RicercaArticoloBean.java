package it.ispw.project.bean;

public class RicercaArticoloBean {

    // Testo libero digitato dall'utente (es. "Rastrello")
    private String testoRicerca;

    // Filtro per categoria (es. "MANGIME") - Null se vuole vedere tutto
    private String tipoArticolo;

    // Filtri per prezzo (uso oggetti Double wrapper per gestire i null)
    private Double prezzoMin;
    private Double prezzoMax;

    public RicercaArticoloBean() {}

    // --- Getters & Setters ---

    public String getTestoRicerca() { return testoRicerca; }
    public void setTestoRicerca(String testoRicerca) { this.testoRicerca = testoRicerca; }

    public String getTipoArticolo() { return tipoArticolo; }
    public void setTipoArticolo(String tipoArticolo) { this.tipoArticolo = tipoArticolo; }

    public Double getPrezzoMin() { return prezzoMin; }
    public void setPrezzoMin(Double prezzoMin) { this.prezzoMin = prezzoMin; }

    public Double getPrezzoMax() { return prezzoMax; }
    public void setPrezzoMax(Double prezzoMax) { this.prezzoMax = prezzoMax; }
}