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

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginGraphicController {

    private static final Logger LOGGER = Logger.getLogger(LoginGraphicController.class.getName());

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
            LOGGER.log(Level.SEVERE, "Errore tecnico durante il login.", e);
            mostraAlert("Errore Sistema", "Impossibile completare il login. Riprova piu' tardi.", Alert.AlertType.ERROR);

        } catch (Exception e) {
            // Caso imprevisto (es. NullPointerException o errori JavaFX)
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante il login.", e);
            mostraAlert("Errore Imprevisto", "Si e' verificato un errore imprevisto. Riprova piu' tardi.", Alert.AlertType.ERROR);
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
    public void onPasswordDimenticataClick() {
        mostraFunzionalitaNonImplementata();
    }

    @FXML
    public void onRegistratiClick() {
        mostraFunzionalitaNonImplementata();
    }

    private void mostraFunzionalitaNonImplementata() {
        mostraAlert("Funzionalita non disponibile",
                "Funzionalità non ancora implementata.",
                Alert.AlertType.INFORMATION);
    }
}
