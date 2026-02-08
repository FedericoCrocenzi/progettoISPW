package it.ispw.project.applicationController;

import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;
import it.ispw.project.model.Utente;
import it.ispw.project.sessionManager.SessionManager;

/**
 * Controller Applicativo Stateless.
 * Gestisce il login senza conoscere il tipo di persistenza utilizzata.
 */
public class LoginControllerApplicativo {

    /**
     * Gestisce il login ricevendo un Bean di input e restituendo un Bean di output.
     */
    public UtenteBean login(LoginBean loginBean)
            throws InvalidCredentialsException, DAOException {

        // 1. Validazione input sul Bean
        if (loginBean == null ||
                loginBean.getUsername() == null ||
                loginBean.getPassword() == null) {

            throw new InvalidCredentialsException("Dati di login mancanti o incompleti.");
        }

        String username = loginBean.getUsername();
        String password = loginBean.getPassword();

        if (username.isBlank() || password.isBlank()) {
            throw new InvalidCredentialsException("Inserire username e password.");
        }

        // 2. Interrogazione DAO (persistenza scelta dalla CLI)
        DAOFactory factory = DAOFactory.getDAOFactory();
        UtenteDAO utenteDAO = factory.getUtenteDAO();

        Utente utenteTrovato = utenteDAO.checkCredentials(username, password);

        // 3. Verifica logica (Errore Business)
        if (utenteTrovato == null) {
            throw new InvalidCredentialsException(
                    "Credenziali errate: Username o Password non validi."
            );
        }

        // 4. Gestione Sessione
        String sessionId = SessionManager.getInstance().addSession(utenteTrovato);

        // 5. Creazione Bean di risposta
        UtenteBean outBean = new UtenteBean();
        outBean.setUsername(utenteTrovato.leggiUsername());
        outBean.setRuolo(utenteTrovato.scopriRuolo());
        outBean.setEmail(utenteTrovato.leggiEmail());
        outBean.setIndirizzo(utenteTrovato.leggiIndirizzo());
        outBean.setSessionId(sessionId);

        return outBean;
    }
}
