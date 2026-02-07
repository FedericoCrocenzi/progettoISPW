package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.model.Carrello;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.sessionManager.SessionManager;
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

import java.io.InputStream;

public class CarrelloGraphicController implements ControllerGraficoBase, Observer {

    @FXML private VBox vboxCarrello; // Il contenitore dentro lo ScrollPane
    @FXML private Label lblTotale;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // Observer: si aggiorna se il carrello cambia (es. da altre finestre)
        Carrello carrelloModel = SessionManager.getInstance().getSession(sessionId).getCarrelloCorrente();
        carrelloModel.attach(this);

        aggiornaVista();
    }

    @Override
    public void update(Object subject) {
        // Aggiornamento thread-safe della UI
        Platform.runLater(this::aggiornaVista);
    }

    private void aggiornaVista() {
        // 1. Recupero dati
        CarrelloBean carrelloBean = appController.visualizzaCarrello();

        // 2. Pulizia Grafica
        vboxCarrello.getChildren().clear();

        // 3. Generazione Card Prodotti
        if (carrelloBean.getListaArticoli().isEmpty()) {
            vboxCarrello.getChildren().add(new Label("Il carrello è vuoto."));
        } else {
            for (ArticoloBean art : carrelloBean.getListaArticoli()) {
                AnchorPane card = creaCardProdotto(art);
                vboxCarrello.getChildren().add(card);
            }
        }

        // 4. Aggiornamento Totale
        lblTotale.setText(String.format("€ %.2f", carrelloBean.getTotale()));
    }

    @FXML
    public void procediAlPagamento() {
        if (vboxCarrello.getChildren().isEmpty() || lblTotale.getText().equals("€ 0.00")) {
            mostraMessaggio("Carrello Vuoto", "Aggiungi articoli prima di pagare.");
            return;
        }
        mostraMessaggio("Pagamento", "Reindirizzamento al modulo di pagamento...");
        // Qui chiameresti: ViewSwitcher.switchTo("PagamentoView.fxml", sessionId);
    }

    // --- METODO CHE GENERA LA GRAFICA (Replica del tuo FXML) ---
    private AnchorPane creaCardProdotto(ArticoloBean art) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(100.0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Immagine
        ImageView imgView = new ImageView();
        imgView.setFitHeight(80.0);
        imgView.setFitWidth(80.0);
        imgView.setLayoutX(8.0);
        imgView.setLayoutY(6.0);
        imgView.setPreserveRatio(true);
        caricaImmagine(imgView, art.getImmaginePath());

        // Nome
        Label lblNome = new Label(art.getDescrizione());
        lblNome.setLayoutX(93.0);
        lblNome.setLayoutY(17.0);

        // Prezzo
        Label lblPrezzo = new Label(String.format("€ %.2f", art.getPrezzo()));
        lblPrezzo.setLayoutX(93.0);
        lblPrezzo.setLayoutY(53.0);
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        // Controlli Quantità
        HBox hBoxQty = new HBox(10);
        hBoxQty.setAlignment(Pos.CENTER);
        hBoxQty.setLayoutX(218.0);
        hBoxQty.setLayoutY(62.0);
        hBoxQty.setPrefSize(89.0, 28.0);
        hBoxQty.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #FFF176; -fx-border-radius: 15; -fx-border-width: 2;");

        Button btnMinus = new Button("-");
        btnMinus.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        btnMinus.setOnAction(e -> mostraMessaggio("Info", "Usa il cestino per rimuovere."));

        Label lblQty = new Label(String.valueOf(art.getQuantita()));
        lblQty.setStyle("-fx-font-weight: bold;");

        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
        btnPlus.setOnAction(e -> {
            try {
                appController.aggiungiArticoloAlCarrello(art, 1);
            } catch (Exception ex) {
                mostraMessaggio("Errore", ex.getMessage());
            }
        });

        hBoxQty.getChildren().addAll(btnMinus, lblQty, btnPlus);

        // Bottone Cestino (Rimuovi)
        Button btnTrash = new Button("X"); // Usa testo o icona
        btnTrash.setLayoutX(285.0);
        btnTrash.setLayoutY(3.0);
        btnTrash.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-cursor: hand;");
        btnTrash.setOnAction(e -> {
            appController.rimuoviArticoloDalCarrello(art);
            // L'Observer richiamerà aggiornaVista() automaticamente
        });

        card.getChildren().addAll(imgView, lblNome, lblPrezzo, hBoxQty, btnTrash);
        return card;
    }

    private void caricaImmagine(ImageView imgView, String path) {
        try {
            if (path == null || path.isEmpty()) path = "/Image/logo1.png";
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) is = getClass().getResourceAsStream("/Image/logo1.png");
            imgView.setImage(new Image(is));
        } catch (Exception e) {
            // ignore
        }
    }

    private void mostraMessaggio(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}