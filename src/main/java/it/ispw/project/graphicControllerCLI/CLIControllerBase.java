package it.ispw.project.graphicControllerCLI;

import java.util.Scanner;

public abstract class CLIControllerBase {

    protected static final Scanner scanner = new Scanner(System.in);

    protected String sessionId;

    public void initData(String sessionId) {
        this.sessionId = sessionId;
    }

    public abstract void show();

    protected void waitForEnter() {
        System.out.println("\nPremi INVIO per continuare...");
        scanner.nextLine();
    }
}
