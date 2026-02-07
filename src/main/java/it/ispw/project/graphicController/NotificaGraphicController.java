package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.exception.DAOException; // Assicurati di importare l'eccezione
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class NotificaGraphicController implements ControllerGraficoBase, Observer {

    @FXML private Label lblTitolo;
    @FXML private Label lblOrdine;
    @FXML private Button btnAzione;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;
    private int idOrdineCorrente;
    private String ruoloUtente;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // 1. REGISTRAZIONE OBSERVER
        GestoreNotifiche.getInstance().attach(this);

        // 2. Recupero Dati Sessione
        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session == null) return;
        this.ruoloUtente = session.getRuolo();

        Ordine ordine = session.getUltimoOrdineCreato();
        if (ordine != null) {
            this.idOrdineCorrente = ordine.leggiId();
            lblOrdine.setText("Ordine n° " + idOrdineCorrente);
        }

        // 3. Configurazione Vista
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            configuraPerCliente();
        } else {
            configuraPerCommesso();
        }
    }

    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    private void configuraPerCliente() {
        lblTitolo.setText("Pagamento Effettuato");
        btnAzione.setText("Sono in negozio");
        btnAzione.setOnAction(event -> {
            // Nota: Se notificaPresenzaInNegozio non lancia DAOException, qui non serve il try-catch.
            // Se lo lanciasse, dovresti aggiungerlo anche qui.
            appController.notificaPresenzaInNegozio(idOrdineCorrente);

            btnAzione.setDisable(true);
            btnAzione.setText("In attesa del commesso...");
            mostraInfo("Notifica Inviata", "Il commesso è stato avvisato.");
        });
    }

    /**
     * MODIFICATO: Aggiunto blocco try-catch per gestire DAOException
     */
    private void configuraPerCommesso() {
        lblTitolo.setText("Gestione Ordine");
        btnAzione.setText("Conferma Merce Pronta");

        btnAzione.setOnAction(event -> {
            try {
                // Questa chiamata lancia DAOException perché aggiorna il DB
                appController.confermaRitiroMerce(idOrdineCorrente);

                btnAzione.setDisable(true);
                btnAzione.setText("Ordine Completato");
                mostraInfo("Conferma Inviata", "Il cliente è stato avvisato.");

            } catch (DAOException e) {
                mostraErrore("Errore di Sistema", "Impossibile aggiornare lo stato dell'ordine: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // --- METODO OBSERVER ---
    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof String) {
                String msg = (String) data;
                gestisciMessaggio(msg);
            }
        });
    }

    private void gestisciMessaggio(String msg) {
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            if (msg.contains("MERCE_PRONTA") && msg.contains(String.valueOf(idOrdineCorrente))) {
                lblTitolo.setText("MERCE PRONTA!");
                btnAzione.setText("Ritira e Chiudi");
                btnAzione.setDisable(false);
                btnAzione.setOnAction(e -> tornaAllaHome());
                mostraInfo("Aggiornamento", "Il commesso ha preparato il tuo ordine!");
            }
        } else {
            if (msg.contains("CLIENTE_IN_NEGOZIO") && msg.contains(String.valueOf(idOrdineCorrente))) {
                lblTitolo.setText("IL CLIENTE È QUI!");
                mostraInfo("Attenzione", "Il cliente dell'ordine #" + idOrdineCorrente + " è arrivato in negozio.");
            }
        }
    }

    @FXML
    public void tornaAllaHome() {
        onClose();
        Stage stage = (Stage) btnAzione.getScene().getWindow();
        ViewSwitcher.switchTo("MainView.fxml", sessionId, stage);
    }

    private void mostraInfo(String titolo, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    private void mostraErrore(String titolo, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}