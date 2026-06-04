package it.ispw.project.dao;

import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
import java.util.List;

public interface ArticoloDAO {
    Articolo selectArticoloById(int id) throws DAOException;
    List<Articolo> selectAllArticoli() throws DAOException;
    boolean updateScorta(Articolo articolo) throws DAOException;
    // Eventuali metodi di ricerca avanzata
    List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) throws DAOException;
}
