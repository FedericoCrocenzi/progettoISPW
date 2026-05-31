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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewSwitcher {

    private static final Logger LOGGER = Logger.getLogger(ViewSwitcher.class.getName());
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
     * Cambia il contenuto centrale senza sostituire la Scene principale.
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
            LOGGER.log(Level.SEVERE, "Errore caricamento vista centrale: " + fxmlPath, e);
        }
    }

    /**
     * Cambia schermata riusando la Scene esistente quando possibile.
     */
    public static void switchTo(String fxmlFileName, String sessionId, Stage stage) {
        try {
            String path = fxmlFileName.startsWith("/") ? fxmlFileName : "/view/" + fxmlFileName;

            FXMLLoader loader = new FXMLLoader(ViewSwitcher.class.getResource(path));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                applicaCss(scene);
                registraTastoFullScreen(scene, stage);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
                applicaCss(scene);
            }

            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore critico cambio scena: " + fxmlFileName, e);
        }
    }

    /**
     * Attiva la logica F11 su una Scene.
     */
    public static void registraTastoFullScreen(Scene scene, Stage stage) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }

    private static void applicaCss(Scene scene) {
        URL cssResource = ViewSwitcher.class.getResource("/style.css");
        if (cssResource == null) {
            return;
        }

        String css = cssResource.toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }
}
