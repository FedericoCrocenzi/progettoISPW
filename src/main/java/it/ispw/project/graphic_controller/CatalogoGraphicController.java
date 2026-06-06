package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.RicercaArticoloBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.QuantitaInsufficienteException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CatalogoGraphicController implements ControllerGraficoBase {

    private static final Logger LOGGER = Logger.getLogger(CatalogoGraphicController.class.getName());
    private static final String IMMAGINE_FALLBACK_PATH = "/image/logo1.png";

    @FXML
    private TilePane tilePaneCatalogo;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    private RicercaArticoloBean filtroCorrente;

    @Override
    public void initData(String sessionId) {
        initData(sessionId, null);
    }

    public void initData(String sessionId, RicercaArticoloBean filtro) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        this.filtroCorrente = filtro;
        caricaProdotti();
    }

    private void caricaProdotti() {
        tilePaneCatalogo.getChildren().clear();
        List<ArticoloBean> listaArticoli;

        try {
            if (filtroCorrente != null && filtroCorrente.getTestoRicerca() != null && !filtroCorrente.getTestoRicerca().isEmpty()) {
                listaArticoli = appController.ricercaArticoli(filtroCorrente);
            } else {
                listaArticoli = appController.visualizzaCatalogo();
            }
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento catalogo.", e);
            tilePaneCatalogo.getChildren().add(new Label("Catalogo non disponibile."));
            mostraMessaggio("Errore Sistema",
                    "Impossibile caricare il catalogo. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (listaArticoli.isEmpty()) {
            tilePaneCatalogo.getChildren().add(new Label("Nessun prodotto trovato."));
            return;
        }

        for (ArticoloBean articolo : listaArticoli) {
            VBox card = creaCardArticolo(articolo);
            tilePaneCatalogo.getChildren().add(card);
        }
    }

    private VBox creaCardArticolo(ArticoloBean articolo) {
        VBox card = new VBox(5);
        card.setPrefSize(200, 250);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5); " +
                "-fx-cursor: hand;");

        card.setOnMouseClicked(event -> apriDettaglioArticolo(articolo));

        Label lblNome = new Label(articolo.getDescrizione());
        lblNome.setStyle("-fx-font-size: 14px;");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(105);
        imgView.setFitWidth(120);
        imgView.setPreserveRatio(true);

        try {
            String imagePath = articolo.getImmaginePath();
            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = IMMAGINE_FALLBACK_PATH;
            }
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is != null) {
                imgView.setImage(new Image(is));
            } else {
                imgView.setImage(new Image(getClass().getResourceAsStream(IMMAGINE_FALLBACK_PATH)));
            }
        } catch (Exception e) {
            try {
                imgView.setImage(new Image(getClass().getResourceAsStream(IMMAGINE_FALLBACK_PATH)));
            } catch (Exception ignored) {
                // Fallback immagine non disponibile: la card resta visibile senza immagine.
            }
        }

        Label lblPrezzo = new Label("€ " + String.format("%.2f", articolo.getPrezzo()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnAggiungi = new Button("Aggiungi al carrello");
        btnAggiungi.getStyleClass().add("bottone-giallo");
        btnAggiungi.setOnAction(event -> {
            event.consume();
            handleAggiungiAlCarrello(articolo);
        });

        card.getChildren().addAll(lblNome, imgView, lblPrezzo, btnAggiungi);
        return card;
    }

    private void apriDettaglioArticolo(ArticoloBean articolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/articoloView.fxml"));
            Parent articoloView = loader.load();

            ArticoloViewGraphicController controller = loader.getController();
            controller.setDatiArticolo(articolo, this.sessionId);

            BorderPane mainLayout = (BorderPane) tilePaneCatalogo.getScene().lookup("#rootLayout");

            if (mainLayout != null) {
                mainLayout.setCenter(articoloView);
            } else {
                LOGGER.warning("Errore: Impossibile trovare il rootLayout (MainView).");
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'apertura del dettaglio prodotto.", e);
            mostraMessaggio("Errore Applicazione", "Impossibile aprire il dettaglio prodotto.", Alert.AlertType.ERROR);
        }
    }

    private void handleAggiungiAlCarrello(ArticoloBean articolo) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Aggiungi al Carrello");
        dialog.setHeaderText("Quanti pezzi di '" + articolo.getDescrizione() + "' vuoi acquistare?");
        dialog.setContentText("Quantità:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qta = Integer.parseInt(qtyStr);

                appController.aggiungiArticoloAlCarrello(sessionId, articolo, qta);

                mostraMessaggio("Successo", "Articolo aggiunto al carrello!", Alert.AlertType.INFORMATION);

            } catch (NumberFormatException e) {
                mostraMessaggio("Errore", "Inserisci un numero valido.", Alert.AlertType.ERROR);
            } catch (QuantitaInsufficienteException e) {
                mostraMessaggio("Scorta Insufficiente", e.getMessage(), Alert.AlertType.WARNING);
            } catch (DAOException e) {
                LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta al carrello.", e);
                mostraMessaggio("Errore Sistema",
                        "Impossibile aggiornare il carrello. Riprova piu' tardi.",
                        Alert.AlertType.ERROR);
            } catch (IllegalArgumentException e) {
                mostraMessaggio("Attenzione", e.getMessage(), Alert.AlertType.WARNING);
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
