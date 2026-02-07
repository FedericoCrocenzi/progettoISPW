package it.ispw.project.graphicController;

import it.ispw.project.applicationController.LoginControllerApplicativo;
import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;
import it.ispw.project.view.ViewSwitcher; // <--- Usiamo il tuo ViewSwitcher
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LoginGraphicController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;

    private final LoginControllerApplicativo loginController = new LoginControllerApplicativo();

    @FXML
    public void onLoginClick(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            // 1. Creazione del Bean (Impacchettamento dati)
            LoginBean credenziali = new LoginBean(username, password);

            // 2. Chiamata al Controller Applicativo
            UtenteBean utenteLoggato = loginController.login(credenziali);

            // 3. Cambio Scena usando ViewSwitcher (Molto più pulito)
            // Recuperiamo lo Stage attuale dall'evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Usiamo il metodo statico che abbiamo sistemato prima
            ViewSwitcher.switchTo("MainView.fxml", utenteLoggato.getSessionId(), stage);

        } catch (InvalidCredentialsException e) {
            // Caso: Credenziali Sbagliate (Popup Giallo/Warning)
            mostraAlert("Login Fallito", e.getMessage(), Alert.AlertType.WARNING);

        } catch (DAOException e) {
            // Caso: Database giù o errore SQL (Popup Rosso/Error)
            mostraAlert("Errore Sistema", "Impossibile contattare il server: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();

        } catch (Exception e) {
            // Caso imprevisto
            mostraAlert("Errore Imprevisto", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostraAlert(String titolo, String contenuto, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    @FXML
    public void onRegistratiClick() {
        System.out.println("Navigazione verso registrazione (Non implementata)...");
    }
}