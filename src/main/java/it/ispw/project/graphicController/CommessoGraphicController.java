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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class CommessoGraphicController implements ControllerGraficoBase, Observer {

    @FXML private TilePane tilePaneOrdini; // Deve corrispondere all'fx:id nel FXML
    @FXML private Label lblNomeUtente;     // Opzionale, se vuoi mostrare il nome

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // 1. Registrazione Observer (Per ricevere notifiche di nuovi ordini)
        GestoreNotifiche.getInstance().attach(this);

        // 2. Caricamento iniziale degli ordini esistenti
        caricaOrdini();
    }

    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    private void caricaOrdini() {
        try {
            // Pulisce la griglia prima di ricaricare
            tilePaneOrdini.getChildren().clear();

            List<OrdineBean> ordini = appController.recuperaOrdiniPendenti();

            if (ordini.isEmpty()) {
                Label emptyLabel = new Label("Nessun ordine da evadere.");
                tilePaneOrdini.getChildren().add(emptyLabel);
                return;
            }

            for (OrdineBean ordine : ordini) {
                aggiungiCardOrdine(ordine);
            }

        } catch (DAOException e) {
            mostraErrore("Errore Sistema", "Impossibile caricare gli ordini: " + e.getMessage());
        }
    }

    /**
     * Crea dinamicamente la grafica per un singolo ordine (la "Card").
     * Replica la struttura VBox definita nel tuo FXML originale.
     */
    private void aggiungiCardOrdine(OrdineBean ordine) {
        // Contenitore Card (VBox)
        VBox card = new VBox(5);
        card.setPrefSize(200, 250);
        card.setAlignment(Pos.CENTER);
        // Stile inline o CSS (meglio CSS se hai le classi pronte)
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        card.setPadding(new Insets(10));

        // 1. Label Numero Ordine
        Label lblId = new Label("Ordine n° " + ordine.getId());
        lblId.setStyle("-fx-font-size: 14px;");

        // 2. Immagine (Icona pacco o simili)
        ImageView imgView = new ImageView();
        imgView.setFitHeight(80);
        imgView.setFitWidth(80);
        imgView.setPreserveRatio(true);
        // Carica un'immagine di default dalle risorse
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/main/resources/Image/icon_ordine.png")));
        } catch (Exception e) {
            // Fallback se l'immagine non si trova
            System.err.println("Immagine ordine non trovata.");
        }

        // 3. Label Prezzo
        Label lblPrezzo = new Label(String.format("Totale: € %.2f", ordine.getTotale()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 4. Bottone "Visualizza Dettagli" (Opzionale)
        Button btnDettagli = new Button("Dettagli");
        btnDettagli.getStyleClass().add("bottone-giallo"); // Usa la tua classe CSS
        btnDettagli.setOnAction(e -> mostraDettagli(ordine));

        // 5. Bottone "Merce Pronta"
        Button btnPronto = new Button("Merce Pronta");
        btnPronto.setStyle("-fx-background-color: #125332; -fx-text-fill: white; -fx-cursor: hand;");
        btnPronto.setOnAction(e -> gestisciOrdinePronto(ordine.getId(), card));

        // Aggiunta elementi alla card
        card.getChildren().addAll(lblId, imgView, lblPrezzo, btnDettagli, btnPronto);

        // Aggiunta card alla griglia
        tilePaneOrdini.getChildren().add(card);
    }

    private void gestisciOrdinePronto(int idOrdine, VBox cardGrafica) {
        try {
            // 1. Chiamata al controller (che può lanciare DAOException)
            appController.confermaRitiroMerce(idOrdine);

            // 2. Aggiornamento UI (solo se non ci sono errori)
            tilePaneOrdini.getChildren().remove(cardGrafica);
            mostraInfo("Successo", "Il cliente dell'ordine #" + idOrdine + " è stato notificato.");

        } catch (DAOException e) {
            // Gestione dell'errore
            mostraErrore("Errore Sistema", "Impossibile aggiornare lo stato dell'ordine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraDettagli(OrdineBean ordine) {
        // Qui potresti aprire un popup con la lista articoli
        mostraInfo("Dettagli Ordine #" + ordine.getId(), "Data: " + ordine.getDataCreazione());
    }

    @FXML
    public void onLogoutClick() {
        onClose(); // Deregistra observer
        Stage stage = (Stage) tilePaneOrdini.getScene().getWindow();
        ViewSwitcher.switchTo("Login.fxml", null, stage); // Null sessionId per logout
    }

    // --- METODO OBSERVER ---
    @Override
    public void update(Object data) {
        // Poiché la notifica arriva da un altro thread, usiamo Platform.runLater
        Platform.runLater(() -> {

            // Caso 1: Arriva un nuovo Ordine (Bean o Model)
            if (data instanceof Ordine) {
                // Ricarichiamo la lista per semplicità
                caricaOrdini();
                mostraInfo("Nuovo Ordine!", "È arrivato un nuovo ordine.");
            }
            // Caso 2: Messaggio testuale (es. "CLIENTE_IN_NEGOZIO")
            else if (data instanceof String) {
                String msg = (String) data;
                if (msg.contains("CLIENTE_IN_NEGOZIO")) {
                    mostraInfo("Attenzione", msg);
                    // Qui potresti evidenziare la card specifica se volessi fare una cosa avanzata
                }
            }
        });
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