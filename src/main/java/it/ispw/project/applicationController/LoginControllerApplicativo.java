package it.ispw.project.applicationController;

import it.ispw.project.bean.LoginBean; // <--- Import del Bean
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException; // <--- Import Eccezione Custom
import it.ispw.project.model.Utente;
import it.ispw.project.sessionManager.SessionManager;

public class LoginControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;

    /**
     * Gestisce il login ricevendo un Bean di input e restituendo un Bean di output.
     */
    public UtenteBean login(LoginBean loginBean) throws InvalidCredentialsException, DAOException {

        // 1. Validazione input sul Bean
        if (loginBean == null || loginBean.getUsername() == null || loginBean.getPassword() == null) {
            throw new InvalidCredentialsException("Dati di login mancanti o incompleti.");
        }

        String username = loginBean.getUsername();
        String password = loginBean.getPassword();

        if (username.isBlank() || password.isBlank()) {
            throw new InvalidCredentialsException("Inserire username e password.");
        }

        // 2. Interrogazione DAO
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        UtenteDAO utenteDAO = factory.getUtenteDAO();

        // Può lanciare DAOException (Errore Sistema)
        Utente utenteTrovato = utenteDAO.checkCredentials(username, password);

        // 3. Verifica logica (Errore Business)
        if (utenteTrovato == null) {
            throw new InvalidCredentialsException("Credenziali errate: Username o Password non validi.");
        }

        // 4. Gestione Sessione
        String sessionId = SessionManager.getInstance().addSession(utenteTrovato);

        // 5. Creazione Bean di risposta
        UtenteBean outBean = new UtenteBean();
        outBean.setUsername(utenteTrovato.leggiUsername());
        outBean.setRuolo(utenteTrovato.scopriRuolo());
        outBean.setEmail(utenteTrovato.leggiEmail());
        outBean.setIndirizzo(utenteTrovato.leggiIndirizzo());
        outBean.setSessionId(sessionId); // Token di sessione

        return outBean;
    }
}