package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.session_manager.Session;
import it.ispw.project.session_manager.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileGraphicController implements ControllerGraficoBase {

    @FXML private Label lblUsername;
    @FXML private Label lblRuolo;
    @FXML private TextField txtEmail;
    @FXML private TextField txtIndirizzo;

    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.appController = new AcquistaArticoloControllerApplicativo();

        Session session = SessionManager.getInstance().getSession(sessionId);

        if (session != null) {
            lblUsername.setText(session.getUsername());
            lblRuolo.setText("Ruolo: " + session.getRuolo());

            try {
                UtenteBean utenteBean = appController.recuperaDatiCliente(session.getUserId());

                if (utenteBean != null) {
                    txtEmail.setText(utenteBean.getEmail());
                    txtIndirizzo.setText(utenteBean.getIndirizzo());
                } else {
                    txtEmail.setText("Dati non disponibili");
                    txtIndirizzo.setText("Dati non disponibili");
                }
            } catch (DAOException e) {
                txtEmail.setText("Errore recupero email");
                txtIndirizzo.setText("Errore recupero indirizzo");
            }
        }
    }

    @FXML
    public void salvaModifiche() {
        String nuovaEmail = txtEmail.getText();

        mostraMessaggio("Profilo Aggiornato",
                "Le modifiche sono state salvate(Simulazione).\nNuova Email: " + nuovaEmail);
    }

    @FXML
    public void eliminaAccount() {
        mostraMessaggio("Attenzione", "Funzionalità 'Elimina Account' non disponibile.");
    }

    private void mostraMessaggio(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}
