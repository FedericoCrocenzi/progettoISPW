package it.ispw.project.graphicController;


import it.ispw.project.bean.UtenteBean;
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

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // Recupero i dati dalla sessione
        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session != null) {
            lblUsername.setText(session.getUsername());
            lblRuolo.setText(session.getRuolo());

            // Nota: Nella classe Session che abbiamo scritto, userId e username sono salvati,
            // ma email e indirizzo erano nel model Utente.
            // Se li vuoi visualizzare qui, devi o aggiungerli alla Session o fare una query.
            // Assumiamo di averli aggiunti alla Session o di fare una query fittizia:
            txtEmail.setText("email.simulata@test.it");
            txtIndirizzo.setText("Via Roma 1 (Simulato)");
        }
    }

    @FXML
    public void salvaModifiche() {
        // Mocking: Fingiamo di salvare
        String nuovaEmail = txtEmail.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profilo Utente");
        alert.setHeaderText("Modifica Profilo");
        alert.setContentText("Le modifiche per l'utente " + lblUsername.getText() + " sono state salvate (Simulazione)!\nNuova email: " + nuovaEmail);
        alert.showAndWait();
    }

    @FXML
    public void eliminaAccount() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Elimina Account");
        alert.setHeaderText("Azione Critica");
        alert.setContentText("Questa funzionalità non è disponibile nella versione Demo.");
        alert.showAndWait();
    }
}