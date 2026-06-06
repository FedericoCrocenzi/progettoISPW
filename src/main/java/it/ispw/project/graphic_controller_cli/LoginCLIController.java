package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.LoginControllerApplicativo;
import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;

public class LoginCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== LOGIN ===");
        CLIPrinter.println("0 - Esci");

        CLIPrinter.print("Username: ");
        String username = leggiLinea();
        if (username == null || "0".equals(username.trim())) {
            chiudiApplicazione();
            return;
        }

        CLIPrinter.print("Password: ");
        String password = leggiLinea();
        if (password == null) {
            chiudiApplicazione();
            return;
        }

        LoginBean bean = new LoginBean();
        bean.setUsername(username);
        bean.setPassword(password);

        try {
            UtenteBean out = new LoginControllerApplicativo().login(bean);

            CLIViewNavigator.setSessionId(out.getSessionId());
            CLIPrinter.println("Login OK. Ruolo: " + out.getRuolo());


            switch (out.getRuolo().toUpperCase()) {
                case "CLIENTE" -> CLIViewNavigator.goToCatalogo();
                case "COMMESSO" -> CLIViewNavigator.goToAreaCommesso();
                default -> throw new IllegalStateException(
                        "Ruolo utente non gestito: " + out.getRuolo()
                );
            }

        } catch (InvalidCredentialsException e) {
            CLIPrinter.println("Credenziali errate.");
            waitForEnter();
            show();
        } catch (DAOException e) {
            CLIPrinter.println("Errore di sistema (DAO).");
            waitForEnter();
            show();
        }
    }
}
