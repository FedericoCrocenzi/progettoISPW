package it.ispw.project.model;

import java.util.Date;

public class Mangime extends Articolo {

    private static final long serialVersionUID = 1L;

    // Attributo specifico del diagramma per "mangimi"
    private Date scadenza;

    public Mangime(int id, String descrizione, double prezzo, int scorta, Date scadenza) {
        super(id, descrizione, prezzo, scorta); // Riutilizzo del costruttore padre
        this.scadenza = scadenza;
    }

    /**
     * Implementazione specifica del metodo astratto.
     * Quando il controller chiamerà articolo.getDettagliSpecifici(),
     * la JVM eseguirà questo codice se l'oggetto è un Mangime.
     */
    @Override
    public String getDettagliSpecifici() {
        return "Categoria: Mangime - Scadenza: " + this.scadenza.toString();
    }

    // Getter e Setter specifici
    public Date getScadenza() {
        return scadenza;
    }

    public void setScadenza(Date scadenza) {
        this.scadenza = scadenza;
    }
}