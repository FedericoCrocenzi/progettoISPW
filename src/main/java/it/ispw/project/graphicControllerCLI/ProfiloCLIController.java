package it.ispw.project.graphicControllerCLI;

import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;

import java.util.Scanner;

public class ProfiloCLIController extends CLIControllerBase {

    @Override
    public void show() {
        System.out.println("=== PROFILO UTENTE ===");

        if (sessionId == null) {
            System.out.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        Session session = SessionManager.getInstance().getSession(sessionId);

        if (session == null) {
            System.out.println("Sessione scaduta.");
            CLIViewNavigator.goToLogin();
            return;
        }

        System.out.println("Username: " + session.getUtenteCorrente().leggiUsername());
        System.out.println("Ruolo: " + session.getUtenteCorrente().scopriRuolo());
        System.out.println();
        System.out.println("1 - Torna al catalogo");
        System.out.println("2 - Logout");

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
                System.out.println("Scelta non valida.");
                show();
                break;
        }
    }
}
