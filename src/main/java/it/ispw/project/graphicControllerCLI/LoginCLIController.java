package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.LoginControllerApplicativo;
import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;

public class LoginCLIController extends CLIControllerBase {

    @Override
    public void show() {
        System.out.println("=== LOGIN ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        LoginBean bean = new LoginBean();
        bean.setUsername(username);
        bean.setPassword(password);

        try {
            UtenteBean out = new LoginControllerApplicativo().login(bean);

            CLIViewNavigator.setSessionId(out.getSessionId());
            System.out.println("Login OK. Ruolo: " + out.getRuolo());

            // FIX: switch su String (ruolo) con literal corretti
            switch (out.getRuolo().toUpperCase()) {
                case "CLIENTE" -> CLIViewNavigator.goToCatalogo();
                case "COMMESSO" -> CLIViewNavigator.goToAreaCommesso();
                default -> throw new IllegalStateException(
                        "Ruolo utente non gestito: " + out.getRuolo()
                );
            }

        } catch (InvalidCredentialsException e) {
            System.out.println("Credenziali errate.");
            waitForEnter();
            show();
        } catch (DAOException e) {
            System.out.println("Errore di sistema (DAO).");
            waitForEnter();
            show();
        }
    }
}
