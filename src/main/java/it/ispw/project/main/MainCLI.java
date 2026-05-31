package it.ispw.project.main;

import it.ispw.project.config.PersistenceConfig;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.graphic_controller_cli.CLIControllerBase;
import it.ispw.project.graphic_controller_cli.CLIPrinter;
import it.ispw.project.graphic_controller_cli.CLIViewNavigator;

public class MainCLI {

    public static void main(String[] args) {

        CLIPrinter.println("=== Avvio applicazione CLI ===");
        CLIPrinter.println("Seleziona il tipo di persistenza:");
        CLIPrinter.println("1 - JDBC (Database)");
        CLIPrinter.println("2 - File System");
        CLIPrinter.println("3 - Demo (In memoria)");

        String input = CLIControllerBase.leggiLinea();
        if (input == null) {
            CLIPrinter.println("Chiusura applicazione.");
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
                CLIPrinter.println("Persistenza JDBC selezionata.");
                break;
            case 2:
                PersistenceConfig.setPersistenceType(DAOFactory.FILESYSTEM);
                CLIPrinter.println("Persistenza File System selezionata.");
                break;
            case 3:
                PersistenceConfig.setPersistenceType(DAOFactory.DEMO);
                CLIPrinter.println("Persistenza Demo selezionata.");
                break;
            default:
                CLIPrinter.println("Scelta non valida. Uso persistenza DEMO di default.");
                PersistenceConfig.setPersistenceType(DAOFactory.DEMO);
                break;
        }

        CLIPrinter.println("--------------------------------");
        CLIViewNavigator.goToLogin();
    }
}
