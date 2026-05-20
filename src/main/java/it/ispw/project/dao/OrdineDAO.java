package it.ispw.project.dao;

import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Ordine;
import java.util.List;

public interface OrdineDAO {
    void insertOrdine(Ordine ordine) throws DAOException;
    Ordine selectOrdineById(int id) throws DAOException;
    void updateStato(Ordine ordine) throws DAOException;

    List<Ordine> findAll() throws DAOException;
    List<Ordine> findByStato(String stato) throws DAOException;
}
