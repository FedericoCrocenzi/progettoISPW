package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommessoGraphicController implements ControllerGraficoBase, Observer {

    @FXML private TilePane tilePaneOrdini;
    @FXML private ToggleGroup menuGroup;
    @FXML private Label lblNomeUtente;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @FXML
    public void initialize() {
        if (menuGroup != null) {
            menuGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    ToggleButton selected = (ToggleButton) newVal;
                    if ("Profilo".equals(selected.getText())) {
                        mostraInfo("Funzionalità Non Disponibile", "La schermata Profilo non è stata ancora implementata.");
                    }
                }
            });
        }
    }

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        // Inizializza il controller applicativo
        this.appController = new AcquistaArticoloControllerApplicativo();

        // REGISTRAZIONE OBSERVER: Si mette in ascolto per i nuovi ordini
        GestoreNotifiche.getInstance().attach(this);

        // Carica lo stato attuale
        caricaOrdini();
    }

    /**
     * Importante: deregistrarsi quando si chiude/logout per evitare memory leak.
     */
    public void onClose() {
        GestoreNotifiche.getInstance().detach(this);
    }

    public void caricaOrdini() {
        if (tilePaneOrdini == null) return;

        try {
            tilePaneOrdini.getChildren().clear();
            List<OrdineBean> ordini = appController.recuperaOrdiniPendenti();

            if (ordini.isEmpty()) {
                tilePaneOrdini.getChildren().add(new Label("Nessun ordine da evadere."));
                return;
            }

            for (OrdineBean ordine : ordini) {
                aggiungiCardOrdine(ordine);
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
            // Assicurati che l'immagine esista o gestisci l'eccezione come fatto qui
            imgView.setImage(new Image(getClass().getResourceAsStream("/Image/icons-logistica.png")));
        } catch (Exception e) { /* Ignora se immagine non trovata */ }

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

    /**
     * Metodo pubblico usato anche dal Popup Notifica per aprire i dettagli.
     */
    public void apriDettaglioOrdine(OrdineBean ordine) {
        mostraDettagli(ordine);
    }

    private void mostraDettagli(OrdineBean ordine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordineCommessoView.fxml"));
            Parent root = loader.load();

            // Passiamo i dati al controller della view di dettaglio
            OrdineCommessoGraphicController controller = loader.getController();
            controller.initData(ordine, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Dettaglio Ordine #" + ordine.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            mostraErrore("Errore GUI", "Impossibile aprire il dettaglio ordine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- GESTIONE POPUP (Usa NotificaCommessoGraphicController) ---

    private void apriPopupNotifica(OrdineBean ordineBean, String titolo, String messaggio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaCommessoView.fxml"));
            Parent root = loader.load();

            // Recuperiamo il controller DEDICATO al popup
            NotificaCommessoGraphicController popupController = loader.getController();

            // Inizializziamo il popup passando:
            // 1. Il Bean dell'ordine (se presente)
            // 2. Un riferimento a 'this' (il padre) per permettere al popup di richiamare apriDettaglioOrdine
            if (ordineBean != null) {
                popupController.initData(ordineBean, this);
            } else {
                // Caso messaggio generico
                popupController.setMessaggioSemplice(titolo, messaggio);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Blocca l'interazione sotto
            stage.setTitle(titolo);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore caricamento popup: " + e.getMessage());
            mostraInfo(titolo, messaggio); // Fallback
        }
    }

    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof Ordine) {
                // 1. Aggiorna la dashboard per mostrare la nuova card in griglia
                caricaOrdini();

                // 2. Prepara i dati per il popup
                Ordine nuovoOrdine = (Ordine) data;
                OrdineBean bean = convertiToBean(nuovoOrdine);

                // 3. Mostra il popup usando il controller dedicato
                apriPopupNotifica(bean, "Nuovo Ordine!", null);

            } else if (data instanceof String) {
                String msg = (String) data;
                if (msg.contains("CLIENTE_IN_NEGOZIO")) {
                    caricaOrdini();
                    // Popup generico informativo
                    apriPopupNotifica(null, "Cliente Arrivato", msg);
                }
            }
        });
    }

    // Helper per convertire Model -> Bean
    private OrdineBean convertiToBean(Ordine o) {
        OrdineBean b = new OrdineBean();
        b.setId(o.leggiId());
        b.setTotale(o.getTotale());
        b.setStato(o.getStato());
        b.setDataCreazione(o.getDataCreazione());

        List<ArticoloBean> articoliBean = new ArrayList<>();
        if (o.getArticoli() != null) {
            for (Map.Entry<Articolo, Integer> entry : o.getArticoli().entrySet()) {
                Articolo a = entry.getKey();
                ArticoloBean ab = new ArticoloBean();
                ab.setId(a.leggiId());
                ab.setDescrizione(a.leggiDescrizione());
                ab.setPrezzo(a.ottieniPrezzo());
                ab.setQuantita(entry.getValue()); // Quantità nell'ordine
                articoliBean.add(ab);
            }
        }
        b.setArticoli(articoliBean);
        return b;
    }

    @FXML
    public void onLogoutClick() {
        onClose(); // Deregistra l'observer
        Stage stage = (Stage) tilePaneOrdini.getScene().getWindow();
        ViewSwitcher.switchTo("/view/Login.fxml", null, stage);
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