package it.ispw.project.dao.Demo;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException; // Import fondamentale
import it.ispw.project.model.Utente;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione in memoria (RAM) del DAO Utente.
 * Utile per testare l'applicazione senza database attivo.
 */
public class MemoryUtenteDAO implements UtenteDAO {

    // Lista statica che simula la tabella del DB
    private static List<Utente> tabellaUtenti = new ArrayList<>();

    static {
        // Utente 1: CLIENTE (Mario Rossi)
        // Login: "cliente" / "123"
        tabellaUtenti.add(new Utente(
                1, "cliente", "123", "CLIENTE", "mario.rossi@email.it", "Via Roma 1"
        ));

        // Utente 2: COMMESSO (Luigi Verdi)
        // Login: "commesso" / "123"
        tabellaUtenti.add(new Utente(
                2, "commesso", "123", "COMMESSO", "luigi.verdi@store.it", "Via Magazzino 4"
        ));
    }

    /**
     * Verifica credenziali (Username O Email) + Password.
     */
    @Override
    public Utente checkCredentials(String identifier, String password) throws DAOException {
        for (Utente u : tabellaUtenti) {
            // Verifica se l'identifier corrisponde a username O email
            boolean matchUserOrEmail = u.leggiUsername().equals(identifier) ||
                    (u.leggiEmail() != null && u.leggiEmail().equals(identifier));

            if (matchUserOrEmail && u.ottieniPassword().equals(password)) {
                return u;
            }
        }
        return null; // Credenziali errate
    }

    /**
     * Recupera utente per ID (Usato da AcquistaArticoloController).
     */
    @Override
    public Utente findById(int id) throws DAOException {
        for (Utente u : tabellaUtenti) {
            if (u.ottieniId() == id) {
                return u;
            }
        }
        return null; // Non trovato
    }

    /**
     * Simula il salvataggio (Registrazione).
     */
    @Override
    public void salva(Utente utente) throws DAOException {
        // Simulazione ID Auto-Increment
        int nuovoId = tabellaUtenti.size() + 1;

        // Creiamo un nuovo oggetto con l'ID assegnato (simulando il comportamento del DB)
        // Nota: Questo richiede un costruttore o un modo per settare l'ID,
        // ma dato che il Model non ha setter per l'ID (correttamente),
        // dobbiamo ricreare l'oggetto o accettare che in memoria l'ID venga gestito così.
        // Per semplicità demo, aggiungiamo direttamente l'oggetto se ha già un ID,
        // oppure ne creiamo una copia con ID aggiornato.

        Utente utenteSalvato = new Utente(
                nuovoId,
                utente.leggiUsername(),
                utente.ottieniPassword(),
                utente.scopriRuolo(),
                utente.leggiEmail(),
                utente.leggiIndirizzo()
        );

        tabellaUtenti.add(utenteSalvato);
    }
}