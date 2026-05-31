package it.ispw.project.dao.Demo;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Magazzino; // Importa il Singleton
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class MemoryArticoloDAO implements ArticoloDAO {

    private static final Logger LOGGER = Logger.getLogger(MemoryArticoloDAO.class.getName());

    // Rimuoviamo la lista statica 'tabellaArticoli'.
    // Usiamo Magazzino come unica fonte di verità.

    public MemoryArticoloDAO() {
        // Opzionale: Se il Magazzino è vuoto, lo popoliamo con dati fake all'avvio.
        Magazzino magazzino = Magazzino.getInstance();
        if (magazzino.getCatalogo().isEmpty()) {
            popolaDatiFake(magazzino);
        }
    }

    private void popolaDatiFake(Magazzino magazzino) {
        // ID 1: Mangime
        magazzino.aggiungiArticolo(new Mangime(1, "Crocchette Premium", 25.50, 100, new Date()));
        // ID 2: Utensile
        magazzino.aggiungiArticolo(new Utensile(2, "Zappa in Acciaio", 15.00, 50, "Acciaio Inox"));
        // ID 3: Fitofarmaco
        magazzino.aggiungiArticolo(new Fitofarmaco(3, "Diserbante Potente", 45.00, 20, true));

        LOGGER.info("DEMO: Magazzino popolato con dati di prova.");
    }

    @Override
    public Articolo selectArticoloById(int id) {
        // Deleghiamo al Magazzino
        return Magazzino.getInstance().trovaArticolo(id);
    }

    @Override
    public List<Articolo> selectAllArticoli() {
        // Recuperiamo la mappa dal Magazzino e la convertiamo in Lista per rispettare l'interfaccia
        return new ArrayList<>(Magazzino.getInstance().getCatalogo().values());
    }

    @Override
    public boolean updateScorta(Articolo articolo) {
        if (articolo == null) {
            return false;
        }

        // In modalità DEMO (tutto in RAM), l'oggetto 'articolo' passato dal Controller
        // è ESATTAMENTE lo stesso oggetto che sta dentro la Map del Magazzino (Java lavora per riferimento).
        // Quindi, se il Controller ha fatto: magazzino.scaricaMerce(id, qta),
        // l'oggetto è già aggiornato.

        // Qui non dobbiamo fare nulla di pratico, ma per simulare un DAO reale potremmo fare:
        Magazzino.getInstance().aggiungiArticolo(articolo); // Sovrascrive/Conferma
        LOGGER.info(() -> "DEMO: Scorta salvata (in RAM) per articolo " + articolo.leggiId());
        return true;
    }

    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        // Filtriamo direttamente sugli oggetti del Magazzino
        List<Articolo> tutti = selectAllArticoli();
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tutti) {
            if (rispettaFiltri(a, descrizione, tipo, min, max)) {
                filtrati.add(a);
            }
        }
        return filtrati;
    }

    private boolean rispettaFiltri(Articolo articolo, String descrizione, String tipo, Double min, Double max) {
        return descrizioneCompatibile(articolo, descrizione)
                && tipoCompatibile(articolo, tipo)
                && prezzoCompatibile(articolo, min, max);
    }

    private boolean descrizioneCompatibile(Articolo articolo, String descrizione) {
        return descrizione == null
                || descrizione.isEmpty()
                || articolo.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase());
    }

    private boolean tipoCompatibile(Articolo articolo, String tipo) {
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

    private boolean prezzoCompatibile(Articolo articolo, Double min, Double max) {
        return (min == null || articolo.ottieniPrezzo() >= min)
                && (max == null || articolo.ottieniPrezzo() <= max);
    }
}
