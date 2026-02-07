package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class CommessoGraphicController implements ControllerGraficoBase, Observer {

    @FXML private TilePane tilePaneOrdini; // Deve corrispondere all'fx:id nel FXML
    @FXML private Label lblNomeUtente;     // Opzionale

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // 1. Registrazione Observer
        GestoreNotifiche.getInstance().attach(this);

        // 2. Caricamento iniziale
        caricaOrdini();
    }

    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    /**
     * Carica gli ordini e controlla se ci sono notifiche pendenti (Stand-alone persistence).
     */
    public void caricaOrdini() {
        try {
            tilePaneOrdini.getChildren().clear();

            List<OrdineBean> ordini = appController.recuperaOrdiniPendenti();

            if (ordini.isEmpty()) {
                Label emptyLabel = new Label("Nessun ordine da evadere.");
                tilePaneOrdini.getChildren().add(emptyLabel);
                return;
            }

            for (OrdineBean ordine : ordini) {
                aggiungiCardOrdine(ordine);

                // CONTROLLO STATO PER NOTIFICA PENDENTE
                // Se il commesso si logga dopo che il cliente ha cliccato "Sono in negozio",
                // lo stato nel DB è già aggiornato. Lo rileviamo qui.
                if ("CLIENTE_IN_NEGOZIO".equals(ordine.getStato())) {
                    apriPopupNotifica("Cliente Arrivato",
                            "Il cliente dell'ordine #" + ordine.getId() + " è in negozio per il ritiro!");
                }
            }

        } catch (DAOException e) {
            mostraErrore("Errore Sistema", "Impossibile caricare gli ordini: " + e.getMessage());
        }
    }

    private void aggiungiCardOrdine(OrdineBean ordine) {
        // ... (Creazione grafica della card identica a prima) ...
        VBox card = new VBox(5);
        card.setPrefSize(200, 250);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        card.setPadding(new Insets(10));

        Label lblId = new Label("Ordine n° " + ordine.getId());
        lblId.setStyle("-fx-font-size: 14px;");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80);
        imgView.setFitWidth(80);
        imgView.setPreserveRatio(true);
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/main/resources/Image/icon_ordine.png")));
        } catch (Exception e) { /* Fallback */ }

        Label lblPrezzo = new Label(String.format("Totale: € %.2f", ordine.getTotale()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // --- BOTTONE DETTAGLI ---
        Button btnDettagli = new Button("Dettagli");
        btnDettagli.getStyleClass().add("bottone-giallo");
        // Ora chiama il metodo che apre il nuovo FXML dettagliato
        btnDettagli.setOnAction(e -> mostraDettagli(ordine));

        Button btnPronto = new Button("Merce Pronta");
        btnPronto.setStyle("-fx-background-color: #125332; -fx-text-fill: white; -fx-cursor: hand;");
        btnPronto.setOnAction(e -> gestisciOrdinePronto(ordine.getId(), card));

        card.getChildren().addAll(lblId, imgView, lblPrezzo, btnDettagli, btnPronto);
        tilePaneOrdini.getChildren().add(card);
    }

    // --- LOGICA DI BUSINESS UI ---

    private void gestisciOrdinePronto(int idOrdine, VBox cardGrafica) {
        try {
            appController.confermaRitiroMerce(idOrdine);
            tilePaneOrdini.getChildren().remove(cardGrafica);
            // Uso una notifica info semplice qui, o il popup se preferisci coerenza
            mostraInfo("Successo", "Stato aggiornato e cliente notificato.");
        } catch (DAOException e) {
            mostraErrore("Errore", e.getMessage());
        }
    }

    /**
     * Apre il file ordineCommessoView.fxml come popup modale.
     */
    private void mostraDettagli(OrdineBean ordine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/view/ordineCommessoView.fxml"));
            Parent root = loader.load();

            // Passaggio dati al controller del dettaglio
            OrdineCommessoGraphicController controller = loader.getController();
            controller.initData(ordine, this); // 'this' serve se vuoi ricaricare la lista alla chiusura

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Blocca la finestra sotto
            stage.setTitle("Dettaglio Ordine #" + ordine.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostraErrore("Errore GUI", "Impossibile aprire il dettaglio ordine.");
            e.printStackTrace();
        }
    }

    /**
     * Apre il file notificaView.fxml come popup di avviso.
     */
    private void apriPopupNotifica(String titolo, String messaggio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/view/notificaView.fxml"));
            Parent root = loader.load();

            // Configura il controller della notifica
            NotificaGraphicController notificaController = loader.getController();
            notificaController.setMessaggio(titolo, messaggio);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Finestra modale (importante per attirare l'attenzione)
            stage.setTitle(titolo);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore caricamento notificaView.fxml: " + e.getMessage());
            // Fallback su alert standard se fallisce il caricamento FXML
            mostraInfo(titolo, messaggio);
        }
    }

    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            // Caso 1: Nuovo ordine creato (Oggetto Ordine)
            if (data instanceof Ordine) {
                caricaOrdini();
                apriPopupNotifica("Nuovo Ordine", "È arrivato un nuovo ordine da gestire!");
            }
            // Caso 2: Messaggio specifico (Stringa)
            else if (data instanceof String) {
                String msg = (String) data;
                if (msg.contains("CLIENTE_IN_NEGOZIO")) {
                    // Ricarica la lista per mostrare eventuali cambiamenti di stato e apre il popup
                    caricaOrdini();
                    apriPopupNotifica("Cliente Arrivato", msg);
                }
            }
        });
    }

    @FXML
    public void onLogoutClick() {
        onClose();
        Stage stage = (Stage) tilePaneOrdini.getScene().getWindow();
        ViewSwitcher.switchTo("Login.fxml", null, stage);
    }

    private void mostraErrore(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setContentText(testo);
        alert.show();
    }

    private void mostraInfo(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setContentText(testo);
        alert.show();
    }
}