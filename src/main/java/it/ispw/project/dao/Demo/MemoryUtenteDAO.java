package it.ispw.project.dao.Demo;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.model.Utente;

import java.util.ArrayList;
import java.util.List;

public class MemoryUtenteDAO implements UtenteDAO {

    private static List<Utente> tabellaUtenti = new ArrayList<>();

    static {
        // Utente 1: CLIENTE (Mario Rossi)
        // Usa: username="cliente", password="123"
        tabellaUtenti.add(new Utente(
                1, "cliente", "123", "CLIENTE", "mario.rossi@email.it", "Via Roma 1"
        ));

        // Utente 2: COMMESSO (Luigi Verdi)
        // Usa: username="commesso", password="123"
        tabellaUtenti.add(new Utente(
                2, "commesso", "123", "COMMESSO", null, null
        ));
    }

    @Override
    public Utente checkCredentials(String username, String password) {
        for (Utente u : tabellaUtenti) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null; // Credenziali errate
    }
}