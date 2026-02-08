package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.exception.QuantitaInsufficienteException;
import it.ispw.project.model.observer.Observer; // Importa solo l'interfaccia, NON l'Entity Carrello
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

public class CarrelloGraphicController implements ControllerGraficoBase, Observer {

    @FXML private VBox vboxCarrello;
    @FXML private Label lblTotale;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    // NOTA: Nessun attributo 'private Carrello carrelloModel' qui. La Boundary non deve conoscerlo.

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // 1. Istanziamo il Controller Applicativo (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // 2. Registrazione Observer tramite il Controller Applicativo
        // Deleghiamo al controller il compito di fare "attach" sul model reale.
        this.appController.registraOsservatoreCarrello(sessionId, this);

        // 3. Prima visualizzazione
        aggiornaVista();
    }

    /**
     * Metodo chiamato dal Subject (Carrello) quando cambia stato.
     * @param subject L'oggetto che ha generato la notifica.
     */
    @Override
    public void update(Object subject) {
        // ARCHITETTURA: Ignoriamo il parametro 'subject' per non doverlo castare a Carrello (Entity).
        // Sappiamo che qualcosa è cambiato, quindi scateniamo un "Pull" dei dati aggiornati tramite il Controller.
        Platform.runLater(this::aggiornaVista);
    }

    /**
     * Metodo privato che recupera lo stato aggiornato sotto forma di BEAN e aggiorna la UI.
     */
    private void aggiornaVista() {
        // PULL DATI: Chiediamo al controller applicativo il Bean del carrello
        CarrelloBean carrelloBean = appController.visualizzaCarrello(sessionId);

        // Pulizia e ricostruzione UI
        vboxCarrello.getChildren().clear();

        if (carrelloBean.getListaArticoli().isEmpty()) {
            Label emptyLabel = new Label("Il carrello è vuoto.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
            vboxCarrello.getChildren().add(emptyLabel);
        } else {
            for (ArticoloBean art : carrelloBean.getListaArticoli()) {
                AnchorPane card = creaCardProdotto(art);
                vboxCarrello.getChildren().add(card);
            }
        }

        // Aggiornamento Totale
        lblTotale.setText(String.format("€ %.2f", carrelloBean.getTotale()));
    }

    @FXML
    public void procediAlPagamento() {
        if (vboxCarrello.getChildren().isEmpty() || lblTotale.getText().equals("€ 0.00")) {
            mostraMessaggio("Carrello Vuoto", "Non puoi procedere al pagamento con il carrello vuoto.", Alert.AlertType.WARNING);
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

        Label lblPrezzo = new Label(String.format("€ %.2f", art.getPrezzo()));
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
        btnMinus.setOnAction(e -> mostraMessaggio("Info", "Usa il tasto cestino per rimuovere l'articolo.", Alert.AlertType.INFORMATION));

        Label lblQty = new Label(String.valueOf(art.getQuantita()));
        lblQty.setStyle("-fx-font-weight: bold;");

        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        btnPlus.setOnAction(e -> {
            try {
                // Modifica tramite Controller. Non chiamiamo aggiornaVista() manualmente qui
                // perché ci penserà l'update() scatenato dal model.
                appController.aggiungiArticoloAlCarrello(sessionId, art, 1);
            } catch (QuantitaInsufficienteException ex) {
                mostraMessaggio("Scorta Insufficiente", ex.getMessage(), Alert.AlertType.WARNING);
            } catch (Exception ex) {
                mostraMessaggio("Errore", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        hBoxQty.getChildren().addAll(btnMinus, lblQty, btnPlus);

        Button btnTrash = new Button("X");
        btnTrash.setLayoutX(285.0);
        btnTrash.setLayoutY(3.0);
        btnTrash.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnTrash.setOnAction(e -> {
            // Modifica tramite Controller. L'aggiornamento grafico avverrà via Observer.
            appController.rimuoviArticoloDalCarrello(sessionId, art);
        });

        card.getChildren().addAll(imgView, lblNome, lblPrezzo, hBoxQty, btnTrash);
        return card;
    }

    private void caricaImmagine(ImageView imgView, String path) {
        try {
            if (path == null || path.isEmpty()) path = "/Image/logo1.png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) is = getClass().getResourceAsStream("/Image/logo1.png");
            if (is != null) imgView.setImage(new Image(is));
        } catch (Exception e) {
            // Ignora eccezioni caricamento img
        }
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    /**
     * Opzionale: Deregistrazione quando si chiude la finestra o si cambia vista.
     */
    public void onClose() {
        if (appController != null) {
            appController.rimuoviOsservatoreCarrello(sessionId, this);
        }
    }
}