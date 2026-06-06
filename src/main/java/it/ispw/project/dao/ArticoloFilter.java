package it.ispw.project.dao;

import it.ispw.project.model.Articolo;

public final class ArticoloFilter {

    private ArticoloFilter() {
        // Utility class.
    }

    public static boolean rispettaDescrizione(Articolo articolo, String descrizione) {
        return descrizioneCompatibile(articolo, descrizione);
    }

    private static boolean descrizioneCompatibile(Articolo articolo, String descrizione) {
        return descrizione == null
                || descrizione.isEmpty()
                || articolo.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase());
    }
}
