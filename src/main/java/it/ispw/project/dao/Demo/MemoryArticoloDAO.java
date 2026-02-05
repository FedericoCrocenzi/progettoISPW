package it.ispw.project.dao.Demo;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemoryArticoloDAO implements ArticoloDAO {

    // Questo è il nostro "Database Finto" in RAM
    private static List<Articolo> tabellaArticoli = new ArrayList<>();

    // Popoliamo il "DB" con dati di prova appena parte il programma
    static {
        // ID 1: Mangime
        tabellaArticoli.add(new Mangime(1, "Crocchette Premium", 25.50, 100, new Date()));

        // ID 2: Utensile
        tabellaArticoli.add(new Utensile(2, "Zappa in Acciaio", 15.00, 50, "Acciaio Inox"));

        // ID 3: Fitofarmaco
        tabellaArticoli.add(new Fitofarmaco(3, "Diserbante Potente", 45.00, 20, true));
    }

    @Override
    public Articolo selectArticoloById(int id) {
        for (Articolo a : tabellaArticoli) {
            if (a.leggiId() == id) {
                return a; // Ritorna l'oggetto in memoria
            }
        }
        return null;
    }

    @Override
    public List<Articolo> selectAllArticoli() {
        // Ritorniamo una copia della lista per sicurezza, o la lista stessa
        return new ArrayList<>(tabellaArticoli);
    }

    @Override
    public void updateScorta(Articolo articolo) {
        // In memoria RAM, Java lavora "per riferimento".
        // Se hai modificato l'oggetto 'articolo' nel controller,
        // è GIA' modificato anche nella lista 'tabellaArticoli'.
        // Quindi qui simuli solo il successo dell'operazione.
        System.out.println("DEMO: Scorta aggiornata in RAM per articolo " + articolo.leggiId());
    }

    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tabellaArticoli) {
            boolean match = true;

            // 1. Filtro Descrizione
            if (descrizione != null && !descrizione.isEmpty()) {
                if (!a.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase())) match = false;
            }

            // 2. Filtro Tipo
            if (match && tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("MANGIME") && !(a instanceof Mangime)) match = false;
                else if (tipo.equals("UTENSILE") && !(a instanceof Utensile)) match = false;
                else if (tipo.equals("FITOFARMACO") && !(a instanceof Fitofarmaco)) match = false;
            }

            // 3. Filtri Prezzo
            if (match && min != null && a.ottieniPrezzo() < min) match = false;
            if (match && max != null && a.ottieniPrezzo() > max) match = false;

            if (match) filtrati.add(a);
        }
        return filtrati;
    }
}