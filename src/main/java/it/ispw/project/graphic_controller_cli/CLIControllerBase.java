package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;

import java.util.Scanner;

public abstract class CLIControllerBase {

    protected static final Scanner scanner = new Scanner(System.in);

    protected String sessionId;

    public void initData(String sessionId) {
        this.sessionId = sessionId;
    }

    public abstract void show();

    public static String leggiLinea() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }

    protected void waitForEnter() {
        CLIPrinter.println("\nPremi INVIO per continuare...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    protected void chiudiApplicazione() {
        CLIPrinter.println("Chiusura applicazione.");
    }

    protected boolean sessioneNonValida(String messaggio) {
        if (sessionId == null) {
            CLIPrinter.println(messaggio);
            CLIViewNavigator.goToLogin();
            return true;
        }
        return false;
    }

    protected AcquistaArticoloControllerApplicativo creaControllerAcquisto() {
        return new AcquistaArticoloControllerApplicativo();
    }

    protected Integer leggiSceltaIntera() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            show();
            return null;
        }
    }
}
