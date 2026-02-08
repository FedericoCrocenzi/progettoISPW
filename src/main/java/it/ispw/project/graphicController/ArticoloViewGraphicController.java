package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
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

public class ArticoloViewGraphicController {

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

    /**
     * Inizializza la vista con i dati dell'articolo cliccato.
     */
    public void setDatiArticolo(ArticoloBean articolo, String sessionId) {
        this.articoloCorrente = articolo;
        this.sessionId = sessionId;

        // CORREZIONE 1: Il costruttore ora è vuoto (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // Popolamento UI
        lblTitolo.setText(articolo.getDescrizione());
        lblPrezzo.setText(String.format("€ %.2f", articolo.getPrezzo()));
        lblDescrizione.setText(articolo.getDescrizione());
        lblQuantita.setText(String.valueOf(quantitaSelezionata));

        // Gestione Immagine
        try {
            if (articolo.getImmaginePath() != null) {
                // Nota: assicurati che il path sia gestito correttamente come risorsa o file
                imgProdotto.setImage(new Image(getClass().getResourceAsStream(articolo.getImmaginePath())));
            }
        } catch (Exception e) {
            System.err.println("Immagine non trovata: " + articolo.getImmaginePath());
        }

        // Gestione Patentino
        if ("FITOFARMACO".equals(articolo.getType()) && articolo.isServePatentino()) {
            lblPatentino.setVisible(true);
        } else {
            lblPatentino.setVisible(false);
        }
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
            // CORREZIONE 2: Passiamo sessionId al metodo, perché il controller non lo conserva
            appController.aggiungiArticoloAlCarrello(sessionId, articoloCorrente, quantitaSelezionata);

            mostraMessaggio("Successo", "Articolo aggiunto al carrello!", Alert.AlertType.INFORMATION);
            chiudiScheda();
        } catch (QuantitaInsufficienteException e) {
            mostraMessaggio("Attenzione", e.getMessage(), Alert.AlertType.WARNING);
        } catch (IllegalArgumentException | DAOException e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void acquistaSubito() {
        aggiungiAlCarrello();
        // Logica futura per switch view
    }

    /**
     * Il tasto "X" funge da tasto "Indietro" per tornare al Catalogo.
     */
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
            e.printStackTrace();
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