package it.ispw.project.main;

import it.ispw.project.config.PersistenceConfig;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.graphicControllerCLI.CLIControllerBase;
import it.ispw.project.graphicControllerCLI.CLIViewNavigator;

public class MainCLI {

    public static void main(String[] args) {

        System.out.println("=== Avvio applicazione CLI ===");
        System.out.println("Seleziona il tipo di persistenza:");
        System.out.println("1 - JDBC (Database)");
        System.out.println("2 - File System");
        System.out.println("3 - Demo (In memoria)");

        String input = CLIControllerBase.leggiLinea();
        if (input == null) {
            System.out.println("Chiusura applicazione.");
            return;
        }

        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            choice = 3;
        }

        switch (choice) {
            case 1:
                PersistenceConfig.setPersistenceType(DAOFactory.JDBC);
                System.out.println("Persistenza JDBC selezionata.");
                break;
            case 2:
                PersistenceConfig.setPersistenceType(DAOFactory.FILESYSTEM);
                System.out.println("Persistenza File System selezionata.");
                break;
            case 3:
                PersistenceConfig.setPersistenceType(DAOFactory.DEMO);
                System.out.println("Persistenza Demo selezionata.");
                break;
            default:
                System.out.println("Scelta non valida. Uso persistenza DEMO di default.");
                PersistenceConfig.setPersistenceType(DAOFactory.DEMO);
                break;
        }

        System.out.println("--------------------------------");
        CLIViewNavigator.goToLogin();
    }
}
