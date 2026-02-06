package it.ispw.project.applicationController;

import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException; // <--- Importante: Importa la tua eccezione
import it.ispw.project.model.Utente;
import it.ispw.project.sessionManager.SessionManager;

public class LoginControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;

    /**
     * Gestisce il login.
     * @throws IllegalArgumentException se i dati sono nulli o le credenziali errate (logica di business).
     * @throws DAOException se c'è un errore tecnico nel DB (errore di sistema).
     */
    public UtenteBean login(String username, String password) throws IllegalArgumentException, DAOException {

        // 1. Validazione input (Logica base)
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username e password non possono essere vuoti.");
        }

        // 2. Interrogazione DAO
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        UtenteDAO utenteDAO = factory.getUtenteDAO();

        // Questa chiamata ora può lanciare DAOException.
        // Non usiamo try-catch qui perché vogliamo che l'errore "salga" alla GUI.
        Utente utenteTrovato = utenteDAO.checkCredentials(username, password);

        // 3. Verifica logica (Utente non trovato = credenziali sbagliate)
        if (utenteTrovato == null) {
            throw new IllegalArgumentException("Credenziali non valide.");
        }

        // 4. GESTIONE SESSIONE (Solo se login OK)
        String sessionId = SessionManager.getInstance().addSession(utenteTrovato);

        // 5. Creazione Bean di risposta
        UtenteBean bean = new UtenteBean();
        // Uso i metodi "parlanti" del model (Information Hiding)
        bean.setUsername(utenteTrovato.leggiUsername());
        bean.setRuolo(utenteTrovato.scopriRuolo());
        bean.setEmail(utenteTrovato.leggiEmail());
        bean.setIndirizzo(utenteTrovato.leggiIndirizzo());

        // Passiamo il token alla GUI (utile per chiamate successive stateless, se previste)
        bean.setSessionId(sessionId);

        return bean;
    }
}