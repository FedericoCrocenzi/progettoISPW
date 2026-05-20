package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.exception.QuantitaInsufficienteException;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarrelloGraphicController implements ControllerGraficoBase, Observer {

    private static final Logger LOGGER = Logger.getLogger(CarrelloGraphicController.class.getName());

    @FXML private VBox vboxCarrello;
    @FXML private Label lblTotale;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        this.appController.registraOsservatoreCarrello(sessionId, this);
        aggiornaVista();
    }

    @Override
    public void update(Object subject) {
        Platform.runLater(this::aggiornaVista);
    }

    private void aggiornaVista() {
        CarrelloBean carrelloBean = appController.visualizzaCarrello(sessionId);

        vboxCarrello.getChildren().clear();

        if (carrelloBean.getListaArticoli().isEmpty()) {
            Label emptyLabel = new Label("Il carrello è vuoto.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
            vboxCarrello.getChildren().add(emptyLabel);
        } else {
            for (ArticoloBean art : carrelloBean.getListaArticoli()) {
                vboxCarrello.getChildren().add(creaCardProdotto(art));
            }
        }

        lblTotale.setText(String.format("EUR %.2f", carrelloBean.getTotale()));
    }

    @FXML
    public void procediAlPagamento() {
        CarrelloBean carrelloBean = appController.visualizzaCarrello(sessionId);
        if (carrelloBean.getListaArticoli().isEmpty()) {
            mostraMessaggio("Carrello Vuoto",
                    "Il carrello è vuoto. Aggiungi almeno un articolo prima di procedere al pagamento.",
                    Alert.AlertType.WARNING);
            return;
        }

        Stage stage = (Stage) vboxCarrello.getScene().getWindow();
        ViewSwitcher.switchTo("/view/PaymentView.fxml", sessionId, stage);
    }

    private AnchorPane creaCardProdotto(ArticoloBean art) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(100.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80.0);
        imgView.setFitWidth(80.0);
        imgView.setLayoutX(8.0);
        imgView.setLayoutY(6.0);
        imgView.setPreserveRatio(true);
        caricaImmagine(imgView, art.getImmaginePath());

        Label lblNome = new Label(art.getDescrizione());
        lblNome.setLayoutX(93.0);
        lblNome.setLayoutY(17.0);
        lblNome.setStyle("-fx-font-size: 14px;");

        Label lblPrezzo = new Label(String.format("EUR %.2f", art.getPrezzo()));
        lblPrezzo.setLayoutX(93.0);
        lblPrezzo.setLayoutY(53.0);
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox hBoxQty = new HBox(10);
        hBoxQty.setAlignment(Pos.CENTER);
        hBoxQty.setLayoutX(218.0);
        hBoxQty.setLayoutY(62.0);
        hBoxQty.setPrefSize(89.0, 28.0);
        hBoxQty.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #FFF176; -fx-border-radius: 15; -fx-border-width: 2;");

        Button btnMinus = new Button("-");
        btnMinus.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        btnMinus.setOnAction(e -> diminuisciQuantita(art));

        Label lblQty = new Label(String.valueOf(art.getQuantita()));
        lblQty.setStyle("-fx-font-weight: bold;");

        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        btnPlus.setOnAction(e -> aumentaQuantita(art));

        hBoxQty.getChildren().addAll(btnMinus, lblQty, btnPlus);

        Button btnTrash = new Button("X");
        btnTrash.setLayoutX(285.0);
        btnTrash.setLayoutY(3.0);
        btnTrash.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnTrash.setOnAction(e -> appController.rimuoviArticoloDalCarrello(sessionId, art));

        card.getChildren().addAll(imgView, lblNome, lblPrezzo, hBoxQty, btnTrash);
        return card;
    }

    private void aumentaQuantita(ArticoloBean art) {
        try {
            appController.aggiungiArticoloAlCarrello(sessionId, art, 1);
        } catch (QuantitaInsufficienteException ex) {
            mostraMessaggio("Scorta Insufficiente", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aumento della quantita nel carrello.", ex);
            mostraMessaggio("Errore",
                    "Impossibile aggiornare il carrello. Riprova più tardi.",
                    Alert.AlertType.ERROR);
        }
    }

    private void diminuisciQuantita(ArticoloBean art) {
        try {
            appController.diminuisciQuantitaArticoloDalCarrello(sessionId, art, 1);
        } catch (IllegalArgumentException ex) {
            mostraMessaggio("Quantita non valida", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore durante la diminuzione della quantita nel carrello.", ex);
            mostraMessaggio("Errore",
                    "Impossibile aggiornare il carrello. Riprova più tardi.",
                    Alert.AlertType.ERROR);
        }
    }

    private void caricaImmagine(ImageView imgView, String path) {
        try {
            if (path == null || path.isEmpty()) path = "/Image/logo1.png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) is = getClass().getResourceAsStream("/Image/logo1.png");
            if (is != null) imgView.setImage(new Image(is));
        } catch (RuntimeException e) {
            LOGGER.log(Level.FINE, "Immagine carrello non disponibile.", e);
        }
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    public void onClose() {
        if (appController != null) {
            appController.rimuoviOsservatoreCarrello(sessionId, this);
        }
    }
}
