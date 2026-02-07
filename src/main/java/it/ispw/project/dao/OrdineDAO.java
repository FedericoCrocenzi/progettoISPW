package it.ispw.project.dao;

import it.ispw.project.model.Ordine;
import java.util.List; // Importa List

public interface OrdineDAO {
    void insertOrdine(Ordine ordine);
    Ordine selectOrdineById(int id);
    void updateStato(Ordine ordine);

    // --- NUOVO METODO ---
    List<Ordine> findAll();
}