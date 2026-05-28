package it.ispw.project.graphicControllerCLI;

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
        System.out.println("\nPremi INVIO per continuare...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    protected void chiudiApplicazione() {
        System.out.println("Chiusura applicazione.");
    }
}
