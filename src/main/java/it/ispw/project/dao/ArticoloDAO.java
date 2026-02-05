package it.ispw.project.dao;

import it.ispw.project.model.Articolo;
import java.util.List;

public interface ArticoloDAO {
    Articolo selectArticoloById(int id);
    List<Articolo> selectAllArticoli();
    void updateScorta(Articolo articolo);
    // Eventuali metodi di ricerca avanzata
    List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max);
}