package it.ispw.project.bean;

import java.util.Date;

public class ArticoloBean {

    // --- Campi Comuni (Base) ---
    private int id;
    private String descrizione;
    private double prezzo;
    private int quantita; // Disponibilità o qta nel carrello

    // --- IL DISCRIMINATORE ---
    // Fondamentale: dice alla View come interpretare i dati (es. "MANGIME", "UTENSILE")
    private String type;

    // --- Campi Specifici (Optionali/Nullable) ---
    // Appartiene a: Mangime
    private Date dataScadenza;

    // Appartiene a: Utensile
    private String materiale;

    // Appartiene a: Fitofarmaco
    private boolean servePatentino;

    // --- Costruttore Vuoto (Best Practice per i Bean) ---
    public ArticoloBean() {}

    // --- Getters e Setters ---

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

    // Campi specifici: gestiti come semplici getter/setter.
    // Sarà il Controller a decidere quali chiamare in fase di riempimento.

    public Date getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(Date dataScadenza) { this.dataScadenza = dataScadenza; }

    public String getMateriale() { return materiale; }
    public void setMateriale(String materiale) { this.materiale = materiale; }

    public boolean isServePatentino() { return servePatentino; }
    public void setServePatentino(boolean servePatentino) { this.servePatentino = servePatentino; }
}