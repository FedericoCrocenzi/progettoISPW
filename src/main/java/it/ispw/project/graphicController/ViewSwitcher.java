package it.ispw.project.graphicController;

import it.ispw.project.graphicController.ControllerGraficoBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

/**
 * Gestore della Navigazione (Page Controller Pattern).
 * Responsabilità: Caricare i file FXML e posizionarli al centro della MainView.
 */
public class ViewSwitcher {

    private static ViewSwitcher instance;
    private BorderPane mainPane; // Il contenitore principale

    private ViewSwitcher() {}

    public static ViewSwitcher getInstance() {
        if (instance == null) {
            instance = new ViewSwitcher();
        }
        return instance;
    }

    // Chiamato dal MainGraphicController all'avvio per registrarsi
    public void setMainPane(BorderPane mainPane) {
        this.mainPane = mainPane;
    }

    /**
     * Carica una view FXML e la imposta al centro del layout.
     * @param fxmlPath Il percorso del file .fxml (es. "/view/CarrelloView.fxml")
     * @param sessionId L'ID di sessione da passare al nuovo controller
     */
    public void switchView(String fxmlPath, String sessionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Passaggio dati al nuovo controller (se eredita da una base comune)
            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            // Cambio schermata effettiva
            if (mainPane != null) {
                mainPane.setCenter(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della vista: " + fxmlPath);
        }
    }
}