package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.exception.DAOException;
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
    @FXML private Label lblOrdine; // Useremo questa label anche per il corpo del messaggio
    @FXML private Button btnAzione;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;
    private int idOrdineCorrente;
    private String ruoloUtente;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // CORREZIONE 1: Costruttore vuoto (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // Recupero sessione per capire chi è l'utente
        Session session = SessionManager.getInstance().getSession(sessionId);
        // Se non c'è sessione (es. popup generico aperto senza contesto), usciamo
        if (session == null) return;

        this.ruoloUtente = session.getRuolo();

        // Se è un CLIENTE, configuriamo la logica di "Sono in negozio"
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            Ordine ordine = session.getUltimoOrdineCreato();
            if (ordine != null) {
                this.idOrdineCorrente = ordine.leggiId();
                lblOrdine.setText("Ordine n° " + idOrdineCorrente);
            }

            // Registriamo l'observer SOLO se è un cliente che deve attendere la risposta
            GestoreNotifiche.getInstance().attach(this);
            configuraPerCliente();
        }
        // Se è un COMMESSO, initData viene chiamato ma la configurazione specifica
        // avverrà tramite setMessaggio() chiamato dal CommessoGraphicController.
    }

    /**
     * Metodo usato per trasformare questa View in un Popup generico.
     * Utile per il Commesso che deve solo leggere un avviso.
     */
    public void setMessaggio(String titolo, String corpoMessaggio) {
        lblTitolo.setText(titolo);
        lblOrdine.setText(corpoMessaggio);

        // Configuriamo il bottone come un semplice "Chiudi" o "OK"
        btnAzione.setText("OK");
        btnAzione.setDisable(false);
        btnAzione.setOnAction(e -> onClose());
    }

    public void onClose() {
        // Deregistra observer solo se era stato registrato
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            GestoreNotifiche.getInstance().detach(this);
        }
        // Chiude la finestra corrente
        Stage stage = (Stage) btnAzione.getScene().getWindow();
        stage.close();
    }

    private void configuraPerCliente() {
        lblTitolo.setText("Pagamento Effettuato");
        btnAzione.setText("Sono in negozio");

        btnAzione.setOnAction(event -> {
            try {
                // Chiama il controller applicativo per aggiornare lo stato DB e notificare il commesso
                appController.segnalaClienteInNegozio(idOrdineCorrente);

                btnAzione.setDisable(true);
                btnAzione.setText("In attesa del commesso...");
                mostraInfo("Notifica Inviata", "Il commesso è stato avvisato del tuo arrivo.");

            } catch (DAOException e) {
                mostraErrore("Errore di Connessione", "Impossibile inviare la notifica: " + e.getMessage());
            }
        });
    }

    // --- METODO OBSERVER (Usato principalmente dal Cliente in attesa) ---
    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof String) {
                String msg = (String) data;
                gestisciMessaggioCliente(msg);
            }
        });
    }

    private void gestisciMessaggioCliente(String msg) {
        // Il cliente attende che il commesso confermi che la merce è pronta (es. "MERCE_PRONTA")
        if ("CLIENTE".equalsIgnoreCase(ruoloUtente)) {
            // Verifica che il messaggio riguardi il proprio ordine (opzionale ma consigliato)
            // Assumiamo che il messaggio contenga l'ID ordine o sia generico per la demo
            if (msg.contains("MERCE_PRONTA") || msg.contains("PRONTO")) {
                lblTitolo.setText("MERCE PRONTA!");
                lblOrdine.setText("Il tuo ordine #" + idOrdineCorrente + " è pronto al banco.");

                btnAzione.setText("Ritira e Chiudi");
                btnAzione.setDisable(false);

                // Al click, torna alla Home
                btnAzione.setOnAction(e -> {
                    onClose();
                    // Recupera lo stage attuale prima che chiuda o usane uno nuovo se necessario
                    // Nota: Qui onClose chiude lo stage popup. Per tornare alla home serve lo stage principale.
                    // In architettura multi-window, il main stage potrebbe essere ancora sotto.
                    // Se questa è l'unica finestra attiva:
                    // ViewSwitcher.switchTo("MainView.fxml", sessionId, new Stage());
                });

                mostraInfo("Aggiornamento", "Il commesso ha preparato il tuo ordine!");
            }
        }
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