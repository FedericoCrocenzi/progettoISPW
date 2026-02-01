package it.ispw.project.model;

/**
 * Rappresenta prodotti chimici per l'agricoltura.
 * Include controlli di sicurezza (patentino).
 */
public class Fitofarmaco extends Articolo {

    private static final long serialVersionUID = 1L;

    // Attributo specifico: determina se serve una licenza speciale per l'acquisto
    private boolean richiedePatentino;

    public Fitofarmaco(int id, String descrizione, double prezzo, int scorta, boolean richiedePatentino) {
        super(id, descrizione, prezzo, scorta);
        this.richiedePatentino = richiedePatentino;
    }

    /**
     * Implementazione polimorfica:
     * Evidenzia se il prodotto è soggetto a restrizioni.
     */
    @Override
    public String getDettagliSpecifici() {
        String avviso = richiedePatentino ? " [RICHIESTO PATENTINO]" : "";
        return "Categoria: Fitofarmaco" + avviso;
    }

    // --- Logica di Business Specifica ---

    public boolean isPericoloso() {
        return richiedePatentino;
    }

    // --- Getter & Setter ---

    public boolean isRichiedePatentino() {
        return richiedePatentino;
    }

    public void setRichiedePatentino(boolean richiedePatentino) {
        this.richiedePatentino = richiedePatentino;
    }
}