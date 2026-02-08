package it.ispw.project.graphicController;

import it.ispw.project.applicationController.LoginControllerApplicativo;
import it.ispw.project.bean.LoginBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.InvalidCredentialsException;
import it.ispw.project.view.ViewSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LoginGraphicController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;

    // Istanza del Controller Applicativo per gestire la logica di login
    private final LoginControllerApplicativo loginController = new LoginControllerApplicativo();

    @FXML
    public void onLoginClick(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            // 1. Creazione del Bean per passare i dati al Controller Applicativo
            LoginBean credenziali = new LoginBean(username, password);

            // 2. Chiamata al Controller Applicativo
            // Restituisce un UtenteBean contenente i dati dell'utente, incluso il RUOLO
            UtenteBean utenteLoggato = loginController.login(credenziali);

            // 3. Logica di indirizzamento in base al Ruolo
            // Determiniamo quale file FXML caricare analizzando il ruolo nel bean
            String fxmlDestinazione;
            String ruolo = utenteLoggato.getRuolo();

            // Controllo difensivo per il ruolo (default a CLIENTE se null)
            if (ruolo == null) {
                ruolo = "CLIENTE";
            }

            // Switch sul ruolo (case-insensitive per sicurezza)
            if (ruolo.equalsIgnoreCase("COMMESSO")) {
                fxmlDestinazione = "commessoView.fxml"; // Dashboard del Commesso
            } else {
                fxmlDestinazione = "MainView.fxml";     // Home del Cliente
            }

            // 4. Cambio Scena usando ViewSwitcher
            // Recuperiamo lo Stage attuale dall'evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Usiamo il metodo statico passando la destinazione calcolata dinamicamente
            ViewSwitcher.switchTo(fxmlDestinazione, utenteLoggato.getSessionId(), stage);

        } catch (InvalidCredentialsException e) {
            // Caso: Credenziali Sbagliate (Popup Giallo/Warning)
            mostraAlert("Login Fallito", e.getMessage(), Alert.AlertType.WARNING);

        } catch (DAOException e) {
            // Caso: Database giù o errore SQL (Popup Rosso/Error)
            mostraAlert("Errore Sistema", "Impossibile contattare il server: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();

        } catch (Exception e) {
            // Caso imprevisto (es. NullPointerException o errori JavaFX)
            mostraAlert("Errore Imprevisto", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Metodo di utility per mostrare i popup di errore/avviso
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
        // Qui potresti aggiungere: ViewSwitcher.switchTo("Registrazione.fxml", null, stage);
    }
}