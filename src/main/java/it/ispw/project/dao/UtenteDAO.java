package it.ispw.project.dao;

import it.ispw.project.model.Utente;

public interface UtenteDAO {
    Utente checkCredentials(String username, String password);
}