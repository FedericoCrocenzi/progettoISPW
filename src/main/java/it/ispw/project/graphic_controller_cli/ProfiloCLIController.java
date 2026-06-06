package it.ispw.project.graphic_controller_cli;

import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.session_manager.Session;
import it.ispw.project.session_manager.SessionManager;

import java.util.Scanner;

public class ProfiloCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== PROFILO UTENTE ===");

        if (sessionId == null) {
            CLIPrinter.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        Session session = SessionManager.getInstance().getSession(sessionId);

        if (session == null) {
            CLIPrinter.println("Sessione scaduta.");
            CLIViewNavigator.goToLogin();
            return;
        }

        try {
            UtenteBean utenteBean = creaControllerAcquisto().recuperaDatiCliente(session.getUserId());

            if (utenteBean != null) {
                CLIPrinter.println("Username: " + utenteBean.getUsername());
                CLIPrinter.println("Ruolo: " + utenteBean.getRuolo());
            } else {
                CLIPrinter.println("Dati profilo non disponibili.");
            }
        } catch (DAOException e) {
            CLIPrinter.println("Errore nel recupero dati profilo: " + e.getMessage());
        }

        CLIPrinter.println();
        CLIPrinter.println("1 - Torna al catalogo");
        CLIPrinter.println("2 - Logout");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                CLIViewNavigator.goToCatalogo();
                break;
            case 2:
                CLIViewNavigator.logout();
                break;
            default:
                CLIPrinter.println("Scelta non valida.");
                show();
                break;
        }
    }
}
