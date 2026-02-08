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

public class MemoryArticoloDAO implements ArticoloDAO {

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

        System.out.println("DEMO: Magazzino popolato con dati di prova.");
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
    public void updateScorta(Articolo articolo) {
        // In modalità DEMO (tutto in RAM), l'oggetto 'articolo' passato dal Controller
        // è ESATTAMENTE lo stesso oggetto che sta dentro la Map del Magazzino (Java lavora per riferimento).
        // Quindi, se il Controller ha fatto: magazzino.scaricaMerce(id, qta),
        // l'oggetto è già aggiornato.

        // Qui non dobbiamo fare nulla di pratico, ma per simulare un DAO reale potremmo fare:
        Magazzino.getInstance().aggiungiArticolo(articolo); // Sovrascrive/Conferma
        System.out.println("DEMO: Scorta salvata (in RAM) per articolo " + articolo.leggiId());
    }

    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        // Filtriamo direttamente sugli oggetti del Magazzino
        List<Articolo> tutti = selectAllArticoli();
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tutti) {
            boolean match = true;

            // Filtro Descrizione
            if (descrizione != null && !descrizione.isEmpty()) {
                if (!a.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase())) match = false;
            }

            // Filtro Tipo
            if (match && tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("MANGIME") && !(a instanceof Mangime)) match = false;
                else if (tipo.equals("UTENSILE") && !(a instanceof Utensile)) match = false;
                else if (tipo.equals("FITOFARMACO") && !(a instanceof Fitofarmaco)) match = false;
            }

            // Filtri Prezzo
            if (match && min != null && a.ottieniPrezzo() < min) match = false;
            if (match && max != null && a.ottieniPrezzo() > max) match = false;

            if (match) filtrati.add(a);
        }
        return filtrati;
    }
}