package it.ispw.project.dao;

import it.ispw.project.model.Ordine;

public interface OrdineDAO {
    void insertOrdine(Ordine ordine);
    Ordine selectOrdineById(int id);
    void updateStato(Ordine ordine);
}