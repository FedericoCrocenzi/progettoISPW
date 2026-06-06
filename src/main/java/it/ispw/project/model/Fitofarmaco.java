package it.ispw.project.model;

/*
 Rappresenta prodotti chimici per l'agricoltura.
 Include controlli di sicurezza (patentino).
 */
public class Fitofarmaco extends Articolo {

    private static final long serialVersionUID = 1L;

    // Attributo specifico: determina se serve un patentino speciale per l'acquisto
    private boolean richiedePatentino;

    public Fitofarmaco(int id, String descrizione, double prezzo, int scorta, boolean richiedePatentino) {
        super(id, descrizione, prezzo, scorta);
        this.richiedePatentino = richiedePatentino;
    }


    @Override
    public String getDettagliSpecifici() {
        String avviso = richiedePatentino ? " [RICHIESTO PATENTINO]" : "";
        return "Categoria: Fitofarmaco" + avviso;
    }



    public boolean isPericoloso() {
        return isRichiedePatentino();
    }



    public boolean isRichiedePatentino() {
        return richiedePatentino;
    }

    public void setRichiedePatentino(boolean richiedePatentino) {
        this.richiedePatentino = richiedePatentino;
    }
}
