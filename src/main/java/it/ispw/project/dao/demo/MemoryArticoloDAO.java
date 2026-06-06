package it.ispw.project.dao.demo;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.ArticoloFilter;
import it.ispw.project.exception.DAOException;
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
    public Articolo selectArticoloById(int id) throws DAOException {
        // Deleghiamo al Magazzino
        return Magazzino.getInstance().trovaArticolo(id);
    }

    @Override
    public List<Articolo> selectAllArticoli() throws DAOException {
        // Recuperiamo la mappa dal Magazzino e la convertiamo in Lista per rispettare l'interfaccia
        return new ArrayList<>(Magazzino.getInstance().getCatalogo().values());
    }

    @Override
    public boolean updateScorta(Articolo articolo) throws DAOException {
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
    public List<Articolo> selectByDescrizione(String descrizione) throws DAOException {
        // Filtriamo direttamente sugli oggetti del Magazzino
        List<Articolo> tutti = selectAllArticoli();
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tutti) {
            if (ArticoloFilter.rispettaDescrizione(a, descrizione)) {
                filtrati.add(a);
            }
        }
        return filtrati;
    }
}
