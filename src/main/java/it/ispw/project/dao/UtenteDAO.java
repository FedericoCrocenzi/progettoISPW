package it.ispw.project.dao;

import it.ispw.project.exception.DAOException; // Importa la tua eccezione
import it.ispw.project.model.Utente;

public interface UtenteDAO {
    Utente checkCredentials(String username, String password) throws DAOException;
    Utente findById(int id) throws DAOException;
    void salva(Utente utente) throws DAOException;
}