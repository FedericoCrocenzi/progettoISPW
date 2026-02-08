package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileGraphicController implements ControllerGraficoBase {

    @FXML private Label lblUsername;
    @FXML private Label lblRuolo;
    @FXML private TextField txtEmail;
    @FXML private TextField txtIndirizzo;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // CORREZIONE: Costruttore vuoto (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // Recupero i dati dalla sessione
        Session session = SessionManager.getInstance().getSession(sessionId);

        if (session != null) {
            // Dati base dalla sessione (Login)
            lblUsername.setText(session.getUsername());
            lblRuolo.setText("Ruolo: " + session.getRuolo());

            // Tentativo di recuperare dati completi dal DB tramite Controller Applicativo
            try {
                // Nota: recuperaDatiCliente accetta l'ID (int), quindi è corretto passare session.getUserId()
                UtenteBean utenteBean = appController.recuperaDatiCliente(session.getUserId());

                if (utenteBean != null) {
                    txtEmail.setText(utenteBean.getEmail());
                    txtIndirizzo.setText(utenteBean.getIndirizzo());
                } else {
                    txtEmail.setText("Dati non disponibili");
                    txtIndirizzo.setText("Dati non disponibili");
                }
            } catch (DAOException e) {
                // Fallback in caso di errore DB
                txtEmail.setText("Errore recupero email");
                txtIndirizzo.setText("Errore recupero indirizzo");
            }
        }
    }

    @FXML
    public void salvaModifiche() {
        String nuovaEmail = txtEmail.getText();
        String nuovoIndirizzo = txtIndirizzo.getText();

        // Qui chiameresti un metodo del controller applicativo tipo:
        // appController.aggiornaProfilo(nuovaEmail, nuovoIndirizzo);

        mostraMessaggio("Profilo Aggiornato",
                "Le modifiche sono state salvate (Simulazione).\nNuova Email: " + nuovaEmail);
    }

    @FXML
    public void eliminaAccount() {
        mostraMessaggio("Attenzione", "Funzionalità 'Elimina Account' non disponibile nella demo.");
    }

    private void mostraMessaggio(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}