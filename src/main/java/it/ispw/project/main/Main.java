package it.ispw.project.main;

import it.ispw.project.view.ViewSwitcher; // Importa ViewSwitcher
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Caricamento Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // --- AGGIUNGI QUESTA RIGA ---
        // Collega il tasto F11 anche alla prima schermata
        ViewSwitcher.registraTastoFullScreen(scene, stage);
        // -----------------------------

        stage.setTitle("AgriCenter Crocenzi");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}