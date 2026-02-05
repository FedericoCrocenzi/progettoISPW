package it.ispw.project.controller.applicationController;

import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.model.Utente;

// CORREZIONE IMPORT: Deve puntare al package giusto e alla classe con la Maiuscola
import it.ispw.project.sessionManager.SessionManager;

public class LoginControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;

    public UtenteBean login(String username, String password) throws IllegalArgumentException {
        // 1. Validazione input
        if (username == null || password == null) throw new IllegalArgumentException("Dati mancanti");

        // 2. Interrogazione DAO
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        UtenteDAO utenteDAO = factory.getUtenteDAO();
        Utente utenteTrovato = utenteDAO.checkCredentials(username, password);

        if (utenteTrovato == null) {
            throw new IllegalArgumentException("Credenziali non valide.");
        }

        // 3. GESTIONE SESSIONE
        // Ora l'import funziona correttamente
        String sessionId = SessionManager.getInstance().addSession(utenteTrovato);

        // 4. Creazione Bean di risposta
        UtenteBean bean = new UtenteBean();
        bean.setUsername(utenteTrovato.getUsername());
        bean.setRuolo(utenteTrovato.getRuolo());
        bean.setEmail(utenteTrovato.getEmail());
        bean.setIndirizzo(utenteTrovato.getIndirizzo());

        // ORA FUNZIONA: Passiamo il token alla GUI
        bean.setSessionId(sessionId);

        return bean;
    }
}