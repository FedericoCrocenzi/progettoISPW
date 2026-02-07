package it.ispw.project.view;

import it.ispw.project.graphicController.ControllerGraficoBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewSwitcher {

    private static ViewSwitcher instance;
    private BorderPane mainPane;

    private ViewSwitcher() {}

    public static ViewSwitcher getInstance() {
        if (instance == null) {
            instance = new ViewSwitcher();
        }
        return instance;
    }

    public void setMainPane(BorderPane mainPane) {
        this.mainPane = mainPane;
    }

    /**
     * Metodo per cambiare il contenuto centrale (per menu laterale all'interno di MainView).
     * Questo metodo non influenza il Full Screen perché non cambia la Scena intera.
     */
    public void switchView(String fxmlPath, String sessionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            if (mainPane != null) {
                mainPane.setCenter(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore switchView: " + fxmlPath);
        }
    }

    /**
     * Metodo per il cambio SCENA COMPLETA (Login -> MainView, MainView -> Payment, ecc.)
     * Mantiene lo stato FullScreen tra una vista e l'altra.
     */
    public static void switchTo(String fxmlFileName, String sessionId, Stage stage) {
        try {
            // 1. SALVA LO STATO ATTUALE PRIMA DI CAMBIARE SCENA
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();

            // Gestione percorso file
            String path = fxmlFileName.startsWith("/") ? fxmlFileName : "/view/" + fxmlFileName;

            FXMLLoader loader = new FXMLLoader(ViewSwitcher.class.getResource(path));
            Parent root = loader.load();

            // Passaggio dati al controller (Init Data)
            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            // Creazione nuova Scena
            Scene scene = new Scene(root);

            // Carica CSS (se presente)
            try {
                scene.getStylesheets().add(ViewSwitcher.class.getResource("/style.css").toExternalForm());
            } catch (Exception e) {
                // Ignora se non trova il css o se il percorso è diverso
            }

            // IMPORTANTE: Registra nuovamente il tasto F11 sulla NUOVA scena
            // Altrimenti nella nuova schermata il tasto smette di funzionare.
            registraTastoFullScreen(scene, stage);

            // Cambio Scena effettivo
            stage.setScene(scene);
            stage.show();

            // 2. RIPRISTINA LO STATO SALVATO
            // Questo va fatto DOPO stage.show(), perché il cambio scena resetta i flag.
            if (wasFullScreen) {
                stage.setFullScreen(true);
            } else if (wasMaximized) {
                // Se non era full screen, controlliamo se era almeno massimizzata
                stage.setMaximized(true);
            }

        } catch (IOException e) {
            System.err.println("Errore critico cambio scena: " + fxmlFileName);
            e.printStackTrace();
        }
    }

    /**
     * Metodo Helper: Attiva la logica F11 su qualsiasi scena
     */
    public static void registraTastoFullScreen(Scene scene, Stage stage) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }
}