package it.ispw.project.main;

import it.ispw.project.view.CLIview;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Caricamento del file FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene); // Imposta la scena

        // --- INIZIO NUOVO CODICE ---
        stage.setTitle("Agricenter Crocenzi"); // Il nuovo titolo
        stage.setMaximized(true);              // Massimizza la finestra
        //stage.setFullScreen(true);          // Lascialo commentato (serve solo per giochi/kiosk)
        stage.show();                          // Mostra la finestra alla fine
    }

    public static void main(String[] args) {
        // LOGICA DI SELEZIONE:
        // Se passiamo "cli" come argomento, parte la console.
        if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            CLIview cli = new CLIview();
            cli.start();
        } else {
            // Altrimenti parte la GUI JavaFX
            launch();
        }
    }
}