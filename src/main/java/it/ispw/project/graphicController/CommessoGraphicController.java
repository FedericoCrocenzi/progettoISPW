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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class CommessoGraphicController implements ControllerGraficoBase, Observer {

    @FXML private TilePane tilePaneOrdini; // Assicurati di aggiungere fx:id="tilePaneOrdini" nell'FXML
    @FXML private ToggleGroup menuGroup;   // Questo si collega al ToggleGroup definito nell'FXML
    @FXML private Label lblNomeUtente;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    /**
     * Metodo chiamato automaticamente da JavaFX dopo il caricamento dell'FXML.
     * Qui colleghiamo la logica del pulsante Profilo.
     */
    @FXML
    public void initialize() {
        if (menuGroup != null) {
            menuGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    ToggleButton selected = (ToggleButton) newVal;
                    // Se viene selezionato "Profilo", mostriamo l'avviso
                    if ("Profilo".equals(selected.getText())) {
                        mostraInfo("Funzionalità Non Disponibile", "La schermata Profilo non è stata ancora implementata.");

                        // Opzionale: Torna automaticamente alla tab "Ordini" per non lasciare l'utente su una tab vuota
                        // if (oldVal != null) menuGroup.selectToggle(oldVal);
                    }
                }
            });
        }
    }

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // CORREZIONE ERRORE: Costruttore vuoto (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // 1. Registrazione Observer
        GestoreNotifiche.getInstance().attach(this);

        // 2. Caricamento iniziale
        caricaOrdini();
    }

    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    /**
     * Carica gli ordini e controlla se ci sono notifiche pendenti.
     */
    public void caricaOrdini() {
        // Verifica di sicurezza se l'FXML non è collegato correttamente
        if (tilePaneOrdini == null) {
            System.err.println("ERRORE: tilePaneOrdini è null. Aggiungi fx:id=\"tilePaneOrdini\" al TilePane nel file FXML.");
            return;
        }

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

                // Controllo notifica pendente
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
            imgView.setImage(new Image(getClass().getResourceAsStream("/Image/icon_ordine.png")));
        } catch (Exception e) {
            // Ignora se l'immagine non si trova
        }

        Label lblPrezzo = new Label(String.format("Totale: € %.2f", ordine.getTotale()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnDettagli = new Button("Dettagli");
        btnDettagli.getStyleClass().add("bottone-giallo");
        btnDettagli.setOnAction(e -> mostraDettagli(ordine));

        Button btnPronto = new Button("Merce Pronta");
        btnPronto.setStyle("-fx-background-color: #125332; -fx-text-fill: white; -fx-cursor: hand;");
        btnPronto.setOnAction(e -> gestisciOrdinePronto(ordine.getId(), card));

        card.getChildren().addAll(lblId, imgView, lblPrezzo, btnDettagli, btnPronto);
        tilePaneOrdini.getChildren().add(card);
    }

    private void gestisciOrdinePronto(int idOrdine, VBox cardGrafica) {
        try {
            appController.confermaRitiroMerce(idOrdine);
            tilePaneOrdini.getChildren().remove(cardGrafica);
            mostraInfo("Successo", "Stato aggiornato e cliente notificato.");
        } catch (DAOException e) {
            mostraErrore("Errore", e.getMessage());
        }
    }

    private void mostraDettagli(OrdineBean ordine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordineCommessoView.fxml"));
            Parent root = loader.load();

            OrdineCommessoGraphicController controller = loader.getController();
            controller.initData(ordine, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Dettaglio Ordine #" + ordine.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostraErrore("Errore GUI", "Impossibile aprire il dettaglio ordine.");
            e.printStackTrace();
        }
    }

    private void apriPopupNotifica(String titolo, String messaggio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaView.fxml"));
            Parent root = loader.load();

            NotificaGraphicController notificaController = loader.getController();
            notificaController.setMessaggio(titolo, messaggio);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titolo);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostraInfo(titolo, messaggio);
        }
    }

    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof Ordine) {
                caricaOrdini();
                apriPopupNotifica("Nuovo Ordine", "È arrivato un nuovo ordine da gestire!");
            } else if (data instanceof String) {
                String msg = (String) data;
                if (msg.contains("CLIENTE_IN_NEGOZIO")) {
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
        // Nota: Assicurati che Login.fxml sia nel percorso corretto
        ViewSwitcher.switchTo("/view/Login.fxml", null, stage);
    }

    private void mostraErrore(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.show();
    }

    private void mostraInfo(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.show();
    }
}