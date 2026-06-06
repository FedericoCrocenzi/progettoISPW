package it.ispw.project.application_controller;

import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;
import it.ispw.project.model.Utente;
import it.ispw.project.session_manager.SessionManager;

public class LoginControllerApplicativo {

    public UtenteBean login(LoginBean loginBean)
            throws InvalidCredentialsException, DAOException {

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

        DAOFactory factory = DAOFactory.getDAOFactory();
        UtenteDAO utenteDAO = factory.getUtenteDAO();

        Utente utenteTrovato = utenteDAO.checkCredentials(username, password);

        if (utenteTrovato == null) {
            throw new InvalidCredentialsException(
                    "Credenziali errate: Username o Password non validi."
            );
        }

        String sessionId = SessionManager.getInstance().addSession(utenteTrovato);

        UtenteBean outBean = new UtenteBean();
        outBean.setId(utenteTrovato.ottieniId());
        outBean.setUsername(utenteTrovato.leggiUsername());
        outBean.setRuolo(utenteTrovato.scopriRuolo());
        outBean.setEmail(utenteTrovato.leggiEmail());
        outBean.setIndirizzo(utenteTrovato.leggiIndirizzo());
        outBean.setSessionId(sessionId);

        return outBean;
    }
}
