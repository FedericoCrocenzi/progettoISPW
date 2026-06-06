package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.LoginControllerApplicativo;
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

    private final LoginControllerApplicativo loginController = new LoginControllerApplicativo();

    @FXML
    public void onLoginClick(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            LoginBean credenziali = new LoginBean(username, password);

            UtenteBean utenteLoggato = loginController.login(credenziali);

            String fxmlDestinazione;
            String ruolo = utenteLoggato.getRuolo();

            if (ruolo == null) {
                ruolo = "CLIENTE";
            }

            if (ruolo.equalsIgnoreCase("COMMESSO")) {
                fxmlDestinazione = "commessoView.fxml";
            } else {
                fxmlDestinazione = "MainView.fxml";
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            ViewSwitcher.switchTo(fxmlDestinazione, utenteLoggato.getSessionId(), stage);

        } catch (InvalidCredentialsException e) {
            mostraAlert("Login Fallito", e.getMessage(), Alert.AlertType.WARNING);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore tecnico durante il login.", e);
            mostraAlert("Errore Sistema", "Impossibile completare il login. Riprova piu' tardi.", Alert.AlertType.ERROR);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante il login.", e);
            mostraAlert("Errore Imprevisto", "Si e' verificato un errore imprevisto. Riprova piu' tardi.", Alert.AlertType.ERROR);
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
