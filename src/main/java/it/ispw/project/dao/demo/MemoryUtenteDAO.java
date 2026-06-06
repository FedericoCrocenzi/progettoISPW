package it.ispw.project.dao.demo;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Utente;

import java.util.ArrayList;
import java.util.List;

public class MemoryUtenteDAO implements UtenteDAO {

    private static List<Utente> tabellaUtenti = new ArrayList<>();

    static {
        tabellaUtenti.add(new Utente(
                1,
                "commesso",
                "1234",
                "COMMESSO",
                "commesso@agricenter.it",
                "Via Negozio 1"
        ));

        tabellaUtenti.add(new Utente(
                2,
                "cliente",
                "1234",
                "CLIENTE",
                "cliente@email.it",
                "Via Cliente 10"
        ));
    }

    @Override
    public Utente checkCredentials(String identifier, String password) throws DAOException {
        for (Utente u : tabellaUtenti) {
            boolean matchUsername = u.leggiUsername().equalsIgnoreCase(identifier);
            boolean matchEmail = u.leggiEmail() != null && u.leggiEmail().equalsIgnoreCase(identifier);
            if ((matchUsername || matchEmail) && u.ottieniPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public Utente findById(int id) throws DAOException {
        for (Utente u : tabellaUtenti) {
            if (u.ottieniId() == id) {
                return u;
            }
        }
        return null;
    }
}
