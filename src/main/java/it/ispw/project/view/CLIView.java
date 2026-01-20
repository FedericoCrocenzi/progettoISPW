package it.ispw.project.view;

import java.util.Scanner;

public class CLIView {

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*********************************");
        System.out.println("* PROGETTO ISPW - CLI MODE    *");
        System.out.println("*********************************");
        System.out.println("Digita qualcosa (o 'exit' per uscire):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Uscita in corso...");
                break;
            }

            // Qui in futuro chiamerai il Controller per fare cose vere
            System.out.println("Hai scritto: " + input.toUpperCase());
        }
    }
}