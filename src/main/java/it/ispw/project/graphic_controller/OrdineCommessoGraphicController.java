package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
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

public class OrdineCommessoGraphicController {

    private static final Logger LOGGER = Logger.getLogger(OrdineCommessoGraphicController.class.getName());

    @FXML
    private Label lblTitoloOrdine;

    @FXML
    private VBox vboxArticoli;

    @FXML
    private Button btnChiudi;

    private OrdineBean ordineCorrente;
    private CommessoGraphicController parentController;
    private AcquistaArticoloControllerApplicativo appController;

    public void initData(OrdineBean ordine, CommessoGraphicController parent) {
        this.ordineCorrente = ordine;
        this.parentController = parent;
        this.appController = new AcquistaArticoloControllerApplicativo();

        if (ordine != null) {
            lblTitoloOrdine.setText("ORDINE N° " + ordine.getId());

            popolaListaArticoli();
        }
    }

    private void popolaListaArticoli() {
        vboxArticoli.getChildren().clear();

        if (ordineCorrente.getArticoli() == null || ordineCorrente.getArticoli().isEmpty()) {
            vboxArticoli.getChildren().add(new Label("Nessun articolo in questo ordine."));
            return;
        }

        for (ArticoloBean articolo : ordineCorrente.getArticoli()) {
            AnchorPane rigaArticolo = creaRigaArticolo(articolo);
            vboxArticoli.getChildren().add(rigaArticolo);
        }
    }

    private AnchorPane creaRigaArticolo(ArticoloBean articolo) {
        AnchorPane anchor = new AnchorPane();
        anchor.setPrefHeight(100.0);
        anchor.setPrefWidth(318.0);
        anchor.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80.0);
        imgView.setFitWidth(80.0);
        imgView.setLayoutX(5.0);
        imgView.setLayoutY(6.0);
        imgView.setPreserveRatio(true);
        caricaImmagineArticolo(imgView, articolo.getImmaginePath());

        Label lblNome = new Label(articolo.getDescrizione());
        lblNome.setLayoutX(95.0);
        lblNome.setLayoutY(13.0);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblPrezzo = new Label(String.format("€ %.2f", articolo.getPrezzo()));
        lblPrezzo.setLayoutX(95.0);
        lblPrezzo.setLayoutY(37.0);
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #125332;");

        HBox hboxQta = new HBox();
        hboxQta.setAlignment(Pos.CENTER);
        hboxQta.setPrefHeight(28.0);
        hboxQta.setPrefWidth(95.0);
        hboxQta.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #FFF176; -fx-border-radius: 15; -fx-border-width: 2;");
        AnchorPane.setBottomAnchor(hboxQta, 10.0);
        AnchorPane.setRightAnchor(hboxQta, 10.0);

        Label lblQta = new Label(articolo.getQuantita() + " pz. richiesti");
        lblQta.setStyle("-fx-font-size: 12px;");
        hboxQta.getChildren().add(lblQta);

        anchor.getChildren().addAll(imgView, lblNome, lblPrezzo, hboxQta);

        return anchor;
    }

    private void caricaImmagineArticolo(ImageView imgView, String path) {
        String imagePath = (path == null || path.isBlank()) ? "/image/logo1.png" : path;
        try {
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is == null) {
                is = getClass().getResourceAsStream("/image/logo1.png");
            }
            if (is != null) {
                imgView.setImage(new Image(is));
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.FINE, "Immagine articolo ordine non disponibile.", e);
        }
    }

    @FXML
    public void onOrdineProntoClick() {
        try {
            if (ordineCorrente == null) return;

            appController.confermaMercePronta(ordineCorrente.getId());

            mostraInfo("Ordine Aggiornato", "L'ordine #" + ordineCorrente.getId() + " è pronto e il cliente è stato notificato.");

            if (parentController != null) {
                parentController.caricaOrdini();
            }

            chiudiFinestra();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento stato ordine da dettaglio.", e);
            mostraErrore("Errore", "Impossibile aggiornare l'ordine. Riprova piu' tardi.");
        }
    }

    @FXML
    public void onChiudiClick() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) btnChiudi.getScene().getWindow();
        stage.close();
    }

    private void mostraInfo(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    private void mostraErrore(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}
