package it.ispw.project.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import it.ispw.project.view.CLIView;


import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Caricamento del file FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setTitle("Progetto ISPW");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // LOGICA DI SELEZIONE:
        // Se passiamo "cli" come argomento, parte la console.
        if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            CLIView cli = new CLIView();
            cli.start();
        } else {
            // Altrimenti parte la GUI JavaFX
            launch();
        }
    }
}