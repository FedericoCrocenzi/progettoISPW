package it.ispw.project.view;

import it.ispw.project.graphicController.ControllerGraficoBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
     * Metodo per cambiare il contenuto centrale (per menu laterale).
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
            System.err.println("Errore nel caricamento della vista: " + fxmlPath);
        }
    }

    /**
     * --- NUOVO METODO STATICO ---
     * Questo risolve l'errore nel PaymentGraphicController.
     * Cambia l'intera scena dello Stage (utile per Login, Payment, o finestre popup).
     */
    public static void switchTo(String fxmlFileName, String sessionId, Stage stage) {
        try {
            // Gestione automatica del percorso se non è stato passato completo
            String path = fxmlFileName.startsWith("/") ? fxmlFileName : "/view/" + fxmlFileName;

            FXMLLoader loader = new FXMLLoader(ViewSwitcher.class.getResource(path));
            Parent root = loader.load();

            // Passaggio dati (Sessione)
            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            // Cambio Scena
            Scene scene = new Scene(root);
            // Carica lo stile CSS globale se necessario
            scene.getStylesheets().add(ViewSwitcher.class.getResource("/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException | NullPointerException e) {
            System.err.println("Errore critico nel cambio scena verso: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}