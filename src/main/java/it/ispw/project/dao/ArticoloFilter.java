package it.ispw.project.dao;

import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

public final class ArticoloFilter {

    private ArticoloFilter() {
        // Utility class.
    }

    public static boolean rispettaFiltri(Articolo articolo, String descrizione, String tipo, Double min, Double max) {
        return descrizioneCompatibile(articolo, descrizione)
                && tipoCompatibile(articolo, tipo)
                && prezzoCompatibile(articolo, min, max);
    }

    private static boolean descrizioneCompatibile(Articolo articolo, String descrizione) {
        return descrizione == null
                || descrizione.isEmpty()
                || articolo.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase());
    }

    private static boolean tipoCompatibile(Articolo articolo, String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            return true;
        }
        if (tipo.equals("MANGIME")) {
            return articolo instanceof Mangime;
        }
        if (tipo.equals("UTENSILE")) {
            return articolo instanceof Utensile;
        }
        if (tipo.equals("FITOFARMACO")) {
            return articolo instanceof Fitofarmaco;
        }
        return true;
    }

    private static boolean prezzoCompatibile(Articolo articolo, Double min, Double max) {
        return (min == null || articolo.ottieniPrezzo() >= min)
                && (max == null || articolo.ottieniPrezzo() <= max);
    }
}
