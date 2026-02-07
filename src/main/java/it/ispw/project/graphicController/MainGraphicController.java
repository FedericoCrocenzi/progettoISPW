package it.ispw.project.graphicController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainGraphicController implements ControllerGraficoBase {

    @FXML private BorderPane rootLayout; // Il contenitore principale
    @FXML private ToggleButton btnHome;
    @FXML private ToggleButton btnCarrello;
    @FXML private ToggleButton btnProfilo;
    @FXML private ToggleGroup menuGroup;

    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        // All'avvio carica la Home (Catalogo)
        mostraHome();
    }

    /**
     * Metodo generico per cambiare la vista centrale mantenendo Top/Bottom bar.
     */
    private void caricaVistaCentrale(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();

            // Inizializza il controller della sotto-vista se necessario
            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            // Sostituisce il centro del BorderPane
            rootLayout.setCenter(vista);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore caricamento vista: " + fxmlPath);
        }
    }

    @FXML
    public void mostraHome() {
        caricaVistaCentrale("/view/CatalogoView.fxml");
        if (btnHome != null) btnHome.setSelected(true);
    }

    @FXML
    public void mostraCarrello() {
        caricaVistaCentrale("/view/CarrelloView.fxml");
        if (btnCarrello != null) btnCarrello.setSelected(true);
    }

    @FXML
    public void mostraProfilo() {
        caricaVistaCentrale("/view/ProfileView.fxml");
        if (btnProfilo != null) btnProfilo.setSelected(true);
    }

    @FXML
    public void logout() {
        // Logica logout...
    }
}