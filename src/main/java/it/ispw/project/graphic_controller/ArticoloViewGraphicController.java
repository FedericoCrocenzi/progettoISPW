package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.QuantitaInsufficienteException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticoloViewGraphicController {

    private static final Logger LOGGER = Logger.getLogger(ArticoloViewGraphicController.class.getName());

    @FXML private Label lblTitolo;
    @FXML private Label lblPrezzo;
    @FXML private Label lblDescrizione;
    @FXML private Label lblPatentino;
    @FXML private Label lblQuantita;
    @FXML private ImageView imgProdotto;

    private ArticoloBean articoloCorrente;
    private int quantitaSelezionata = 1;
    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    public void setDatiArticolo(ArticoloBean articolo, String sessionId) {
        this.articoloCorrente = articolo;
        this.sessionId = sessionId;

        this.appController = new AcquistaArticoloControllerApplicativo();

        lblTitolo.setText(articolo.getDescrizione());
        lblPrezzo.setText(String.format("€ %.2f", articolo.getPrezzo()));
        lblDescrizione.setText(articolo.getDescrizione());
        lblQuantita.setText(String.valueOf(quantitaSelezionata));

        try {
            if (articolo.getImmaginePath() != null) {
                imgProdotto.setImage(new Image(getClass().getResourceAsStream(articolo.getImmaginePath())));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e,
                    () -> MessageFormat.format("Immagine non trovata: {0}", articolo.getImmaginePath()));
        }

        lblPatentino.setVisible("FITOFARMACO".equals(articolo.getType()) && articolo.isServePatentino());
    }

    @FXML
    public void aumentaQuantita() {
        quantitaSelezionata++;
        lblQuantita.setText(String.valueOf(quantitaSelezionata));
    }

    @FXML
    public void diminuisciQuantita() {
        if (quantitaSelezionata > 1) {
            quantitaSelezionata--;
            lblQuantita.setText(String.valueOf(quantitaSelezionata));
        }
    }

    @FXML
    public void aggiungiAlCarrello() {
        try {
            appController.aggiungiArticoloAlCarrello(sessionId, articoloCorrente, quantitaSelezionata);

            mostraMessaggio("Successo", "Articolo aggiunto al carrello!", Alert.AlertType.INFORMATION);
            chiudiScheda();
        } catch (QuantitaInsufficienteException e) {
            mostraMessaggio("Attenzione", e.getMessage(), Alert.AlertType.WARNING);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta al carrello.", e);
            mostraMessaggio("Errore Sistema",
                    "Impossibile aggiornare il carrello. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void acquistaSubito() {
        aggiungiAlCarrello();
    }

    @FXML
    public void chiudiScheda() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CatalogoView.fxml"));
            Parent catalogoNode = loader.load();

            CatalogoGraphicController controller = loader.getController();
            controller.initData(sessionId);

            BorderPane mainLayout = (BorderPane) lblTitolo.getScene().lookup("#rootLayout");
            if (mainLayout != null) {
                mainLayout.setCenter(catalogoNode);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della vista catalogo.", e);
        }
    }

    private void mostraMessaggio(String titolo, String contenuto, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}
