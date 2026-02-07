package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.observer.Observer; // Importa la tua interfaccia Observer
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
        // Mi iscrivo per ricevere notifiche (sia se sono Cliente, sia se sono Commesso)
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

    // Importante: DEREGISTRAZIONE quando chiudi la finestra per evitare memory leaks
    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    private void configuraPerCliente() {
        lblTitolo.setText("Pagamento Effettuato");
        btnAzione.setText("Sono in negozio");
        btnAzione.setOnAction(event -> {
            appController.notificaPresenzaInNegozio(idOrdineCorrente);
            btnAzione.setDisable(true);
            btnAzione.setText("In attesa del commesso...");
            mostraInfo("Notifica Inviata", "Il commesso è stato avvisato.");
        });
    }

    private void configuraPerCommesso() {
        lblTitolo.setText("Gestione Ordine");
        btnAzione.setText("Conferma Merce Pronta");
        btnAzione.setOnAction(event -> {
            appController.confermaRitiroMerce(idOrdineCorrente);
            btnAzione.setDisable(true);
            btnAzione.setText("Ordine Completato");
            mostraInfo("Conferma Inviata", "Il cliente è stato avvisato.");
        });
    }

    // --- METODO OBSERVER ---
    @Override
    public void update(Object data) {
        // Poiché l'aggiornamento arriva da un altro thread o contesto,
        // usiamo Platform.runLater per modificare la GUI JavaFX in sicurezza.
        Platform.runLater(() -> {
            if (data instanceof String) {
                String msg = (String) data;
                gestisciMessaggio(msg);
            }
        });
    }

    private void gestisciMessaggio(String msg) {
        // Logica di reazione alle notifiche
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            // Sono CLIENTE: Mi interessa se la merce è pronta
            if (msg.contains("MERCE_PRONTA") && msg.contains(String.valueOf(idOrdineCorrente))) {
                lblTitolo.setText("MERCE PRONTA!");
                btnAzione.setText("Ritira e Chiudi");
                btnAzione.setDisable(false);
                btnAzione.setOnAction(e -> tornaAllaHome());
                mostraInfo("Aggiornamento", "Il commesso ha preparato il tuo ordine!");
            }
        } else {
            // Sono COMMESSO: Mi interessa se il cliente è arrivato
            if (msg.contains("CLIENTE_IN_NEGOZIO") && msg.contains(String.valueOf(idOrdineCorrente))) {
                lblTitolo.setText("IL CLIENTE È QUI!");
                mostraInfo("Attenzione", "Il cliente dell'ordine #" + idOrdineCorrente + " è arrivato in negozio.");
            }
        }
    }

    @FXML
    public void tornaAllaHome() {
        onClose(); // Deregistro l'observer
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
}