package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.QuantitaInsufficienteException;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.InputStream; // Import necessario per gestire lo stream immagine
import java.util.List;
import java.util.Optional;

public class CatalogoGraphicController implements ControllerGraficoBase {

    @FXML
    private TilePane tilePaneCatalogo;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);
        caricaProdotti();
    }

    private void caricaProdotti() {
        try {
            tilePaneCatalogo.getChildren().clear();
            List<ArticoloBean> listaArticoli = appController.visualizzaCatalogo();

            if (listaArticoli.isEmpty()) {
                tilePaneCatalogo.getChildren().add(new Label("Nessun prodotto disponibile."));
                return;
            }

            for (ArticoloBean articolo : listaArticoli) {
                VBox card = creaCardArticolo(articolo);
                tilePaneCatalogo.getChildren().add(card);
            }

        } catch (DAOException e) {
            mostraMessaggio("Errore", "Impossibile caricare il catalogo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox creaCardArticolo(ArticoloBean articolo) {
        VBox card = new VBox(5);
        card.setPrefSize(200, 250);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");

        // 1. Nome Prodotto
        Label lblNome = new Label(articolo.getDescrizione());
        lblNome.setStyle("-fx-font-size: 14px;");

        // 2. Immagine (GESTIONE DINAMICA)
        ImageView imgView = new ImageView();
        imgView.setFitHeight(105);
        imgView.setFitWidth(120);
        imgView.setPreserveRatio(true);

        try {
            // Recupera il percorso dal Bean (es: "/Image/Fieno_per_Conigli_(5kg).png")
            String imagePath = articolo.getImmaginePath();

            // Fallback se il path nel DB è nullo o vuoto
            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = "/Image/logo1.png"; // Immagine di default
            }

            // Tentativo di caricamento dalle risorse
            InputStream is = getClass().getResourceAsStream(imagePath);

            if (is != null) {
                // Immagine trovata
                imgView.setImage(new Image(is));
            } else {
                // File non trovato nelle risorse (es. nome errato), carico fallback
                // System.err.println("Immagine mancante: " + imagePath); // Debug
                imgView.setImage(new Image(getClass().getResourceAsStream("/Image/logo1.png")));
            }

        } catch (Exception e) {
            // Qualsiasi altro errore, carico fallback
            imgView.setImage(new Image(getClass().getResourceAsStream("/Image/logo1.png")));
        }

        // 3. Prezzo
        Label lblPrezzo = new Label("€ " + String.format("%.2f", articolo.getPrezzo()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 4. Bottone
        Button btnAggiungi = new Button("Aggiungi al carrello");
        btnAggiungi.getStyleClass().add("bottone-giallo");
        btnAggiungi.setOnAction(event -> handleAggiungiAlCarrello(articolo));

        card.getChildren().addAll(lblNome, imgView, lblPrezzo, btnAggiungi);
        return card;
    }

    private void handleAggiungiAlCarrello(ArticoloBean articolo) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Aggiungi al Carrello");
        dialog.setHeaderText("Quanti pezzi vuoi acquistare?");
        dialog.setContentText("Quantità:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qta = Integer.parseInt(qtyStr);
                appController.aggiungiArticoloAlCarrello(articolo, qta);
                mostraMessaggio("Successo", "Articolo aggiunto al carrello!", Alert.AlertType.INFORMATION);

            } catch (NumberFormatException e) {
                mostraMessaggio("Errore", "Inserisci un numero valido.", Alert.AlertType.ERROR);
            } catch (QuantitaInsufficienteException e) {
                mostraMessaggio("Scorta Insufficiente", e.getMessage(), Alert.AlertType.WARNING);
            } catch (IllegalArgumentException e) {
                mostraMessaggio("Attenzione", e.getMessage(), Alert.AlertType.WARNING);
            } catch (DAOException e) {
                mostraMessaggio("Errore Sistema", "Problema col database: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}