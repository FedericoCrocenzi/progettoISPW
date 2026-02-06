package it.ispw.project.graphicController;

import it.ispw.project.bean.UtenteBean;
import it.ispw.project.applicationController.LoginControllerApplicativo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginGraphicController {

    @FXML
    private TextField txtUsername; // Corrisponde all'id nel FXML

    @FXML
    private TextField txtPassword; // Corrisponde all'id nel FXML

    // Riferimento al Controller Applicativo
    private final LoginControllerApplicativo loginController = new LoginControllerApplicativo();

    @FXML
    public void onLoginClick(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostraErrore("Campi obbligatori", "Inserisci username e password.");
            return;
        }

        try {
            // 1. Chiamata al Controller Applicativo
            UtenteBean utenteLoggato = loginController.login(username, password);

            // 2. Recupero il SessionID generato
            String sessionId = utenteLoggato.getSessionId();

            // 3. Cambio Scena: Carico la MainView
            caricaMainView(event, sessionId);

        } catch (IllegalArgumentException e) {
            // Credenziali errate o utente non trovato
            mostraErrore("Login Fallito", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostraErrore("Errore di Sistema", "Impossibile completare il login.");
        }
    }

    private void caricaMainView(ActionEvent event, String sessionId) throws IOException {
        // Carico il file FXML della schermata principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        // 4. PASSO FONDAMENTALE: Passo il sessionId al MainGraphicController
        // Questo permetterà alla MainView di inizializzare il ViewSwitcher e sapere chi è l'utente
        MainGraphicController mainController = loader.getController();
        mainController.initData(sessionId);

        // 5. Sostituisco la scena nello Stage attuale
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        // Opzionale: Carico il CSS globale se serve
        // scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("AgriCenter Crocenzi - Home");
        stage.show();

        // Centriamo la finestra (opzionale)
        stage.centerOnScreen();
    }

    private void mostraErrore(String titolo, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(titolo);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    @FXML
    public void onRegistratiClick() {
        // Metodo placeholder se volessi implementare il link "Non hai un account? Registrati"
        // Caricherebbe la RegistrationView.fxml
        System.out.println("Navigazione verso registrazione...");
    }
}