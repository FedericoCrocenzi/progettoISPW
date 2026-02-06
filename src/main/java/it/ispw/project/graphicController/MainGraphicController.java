package it.ispw.project.graphicController;

import it.ispw.project.view.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

public class MainGraphicController implements ControllerGraficoBase {

    @FXML private BorderPane rootLayout; // Il BorderPane principale nel tuo FXML
    @FXML private ToggleButton btnHome;
    @FXML private ToggleButton btnCarrello;
    @FXML private ToggleButton btnProfilo;
    @FXML private ToggleGroup menuGroup; // Associa i bottoni a un gruppo nel FXML

    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // 1. Configura il ViewSwitcher con il pannello centrale di questa finestra
        ViewSwitcher.getInstance().setMainPane(rootLayout);

        // 2. Carica la Home di default
        mostraHome();
    }

    @FXML
    public void mostraHome() {
        // Carica la schermata catalogo prodotti
        ViewSwitcher.getInstance().switchView("/view/CatalogoView.fxml", sessionId);
    }

    @FXML
    public void mostraCarrello() {
        ViewSwitcher.getInstance().switchView("/view/CarrelloView.fxml", sessionId);
    }

    @FXML
    public void mostraProfilo() {
        ViewSwitcher.getInstance().switchView("/view/ProfileView.fxml", sessionId);
    }

    @FXML
    public void logout() {
        // Qui si dovrebbe tornare alla schermata di Login
        // SessionManager.getInstance().removeSession(sessionId);
        // Stage stage = (Stage) rootLayout.getScene().getWindow();
        // ... logica per caricare LoginView.fxml ...
        System.out.println("Logout effettuato (Simulato)");
    }
}