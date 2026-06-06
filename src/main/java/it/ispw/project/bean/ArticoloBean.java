package it.ispw.project.bean;

import java.util.Date;

public class ArticoloBean {


    private int id;
    private String descrizione;
    private double prezzo;
    private int quantita; // Disponibilità o qta nel carrello
    private String immaginePath; // Nuovo attributo

    // Campo che dice alla View come interpretare i dati (es. "MANGIME", "UTENSILE")
    private String type;


    // Appartiene a: Mangime
    private Date dataScadenza;

    // Appartiene a: Utensile
    private String materiale;

    // Appartiene a: Fitofarmaco
    private boolean servePatentino;



    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public double getPrezzo() { return prezzo; }
    public void setPrezzo(double prezzo) { this.prezzo = prezzo; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }



    public Date getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(Date dataScadenza) { this.dataScadenza = dataScadenza; }

    public String getMateriale() { return materiale; }
    public void setMateriale(String materiale) { this.materiale = materiale; }

    public boolean isServePatentino() { return servePatentino; }
    public void setServePatentino(boolean servePatentino) { this.servePatentino = servePatentino; }

    public String getImmaginePath() {
        return immaginePath;
    }

    public void setImmaginePath(String immaginePath) {
        this.immaginePath = immaginePath;
}}
