package it.ispw.project.graphicControllerCLI;

import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;

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

        CLIPrinter.println("Username: " + session.getUtenteCorrente().leggiUsername());
        CLIPrinter.println("Ruolo: " + session.getUtenteCorrente().scopriRuolo());
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
