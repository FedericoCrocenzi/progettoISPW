package it.ispw.project.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Caricamento del file FXML di Login
            // ATTENZIONE: Il percorso deve iniziare con "/" e riferirsi alla cartella resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();

            // 2. Creazione della Scena
            Scene scene = new Scene(root);

            // Opzionale: Se vuoi forzare il caricamento del CSS globalmente
            // scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            // 3. Configurazione dello Stage (Finestra)
            primaryStage.setTitle("AgriCenter Crocenzi - Benvenuto");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Blocchiamo il ridimensionamento per il Login

            // 4. Mostra la finestra
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("ERRORE GRAVE: Impossibile caricare l'interfaccia grafica.");
            System.err.println("Verifica che il file 'Login.fxml' sia nella cartella corretta (src/main/resources/view).");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Avvia il ciclo di vita dell'applicazione JavaFX
        launch(args);
    }
}