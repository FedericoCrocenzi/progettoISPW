package it.ispw.project.bean;

import java.util.Date;
import java.util.List;

public class OrdineBean {

    private int id;
    private Date dataCreazione;
    private double totale;
    private String stato; // Es: "IN_ELABORAZIONE", "PRONTO"

    // Chi ha fatto l'ordine?
    private UtenteBean cliente;

    // Cosa ha comprato?
    private List<ArticoloBean> articoli;

    public OrdineBean() {}

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(Date dataCreazione) { this.dataCreazione = dataCreazione; }

    public double getTotale() { return totale; }
    public void setTotale(double totale) { this.totale = totale; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public UtenteBean getCliente() { return cliente; }
    public void setCliente(UtenteBean cliente) { this.cliente = cliente; }

    public List<ArticoloBean> getArticoli() { return articoli; }
    public void setArticoli(List<ArticoloBean> articoli) { this.articoli = articoli; }
}