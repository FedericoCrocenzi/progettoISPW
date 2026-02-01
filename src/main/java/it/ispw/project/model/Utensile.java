package it.ispw.project.model;

/**
 * Rappresenta un utensile agricolo (es. zappa, rastrello).
 * Estende Articolo ereditandone stato e comportamento base.
 */
public class Utensile extends Articolo {

    private static final long serialVersionUID = 1L;

    // Attributo specifico per Utensile
    private String materiale;

    /**
     * Costruttore: usa 'super' per inizializzare la parte comune (Articolo)
     * e poi inizializza la parte specifica.
     */
    public Utensile(int id, String descrizione, double prezzo, int scorta, String materiale) {
        super(id, descrizione, prezzo, scorta);
        this.materiale = materiale;
    }

    /**
     * Implementazione polimorfica:
     * Restituisce i dettagli focalizzandosi sul materiale.
     */
    @Override
    public String getDettagliSpecifici() {
        return "Categoria: Utensile - Materiale: " + this.materiale;
    }

    // --- Getter & Setter ---

    public String getMateriale() {
        return materiale;
    }

    public void setMateriale(String materiale) {
        this.materiale = materiale;
    }
}