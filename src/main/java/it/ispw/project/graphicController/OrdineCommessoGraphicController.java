package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

public class OrdineCommessoGraphicController {

    @FXML
    private Label lblTitoloOrdine;

    @FXML
    private VBox vboxArticoli;

    @FXML
    private Button btnChiudi;

    @FXML
    private Button btnOrdinePronto;

    private OrdineBean ordineCorrente;
    private CommessoGraphicController parentController;
    private AcquistaArticoloControllerApplicativo appController;

    /**
     * Metodo chiamato dal CommessoGraphicController per passare i dati.
     */
    public void initData(OrdineBean ordine, CommessoGraphicController parent) {
        this.ordineCorrente = ordine;
        this.parentController = parent;
        this.appController = new AcquistaArticoloControllerApplicativo(); // Controller Applicativo Stateless

        if (ordine != null) {
            // 1. Imposta il Titolo
            lblTitoloOrdine.setText("ORDINE N° " + ordine.getId());

            // 2. Popola la lista degli articoli
            popolaListaArticoli();
        }
    }

    private void popolaListaArticoli() {
        // Pulisce eventuali placeholder presenti nell'FXML
        vboxArticoli.getChildren().clear();

        if (ordineCorrente.getArticoli() == null || ordineCorrente.getArticoli().isEmpty()) {
            vboxArticoli.getChildren().add(new Label("Nessun articolo in questo ordine."));
            return;
        }

        // Per ogni articolo nel bean, crea una riga grafica
        for (ArticoloBean articolo : ordineCorrente.getArticoli()) {
            AnchorPane rigaArticolo = creaRigaArticolo(articolo);
            vboxArticoli.getChildren().add(rigaArticolo);
        }
    }

    /**
     * Crea graficamente il box per un singolo articolo.
     * Replica lo stile definito nel tuo FXML originale.
     */
    private AnchorPane creaRigaArticolo(ArticoloBean articolo) {
        AnchorPane anchor = new AnchorPane();
        anchor.setPrefHeight(100.0);
        anchor.setPrefWidth(318.0);
        anchor.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Immagine Prodotto
        ImageView imgView = new ImageView();
        imgView.setFitHeight(80.0);
        imgView.setFitWidth(80.0);
        imgView.setLayoutX(5.0);
        imgView.setLayoutY(6.0);
        imgView.setPreserveRatio(true);
        // Carica immagine o placeholder
        try {
            // Se nel bean c'è un path immagine, usalo, altrimenti usa default
            // String path = articolo.getImmaginePath() != null ? articolo.getImmaginePath() : "/Image/logo1.png";
            imgView.setImage(new Image(getClass().getResourceAsStream("/Image/logo1.png")));
        } catch (Exception e) {
            // Ignora errori di caricamento immagine
        }

        // Nome Articolo
        Label lblNome = new Label(articolo.getDescrizione());
        lblNome.setLayoutX(95.0);
        lblNome.setLayoutY(13.0);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Prezzo
        Label lblPrezzo = new Label(String.format("€ %.2f", articolo.getPrezzo()));
        lblPrezzo.setLayoutX(95.0);
        lblPrezzo.setLayoutY(37.0);
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #125332;");

        // Box Quantità (Badge Giallo)
        HBox hboxQta = new HBox();
        hboxQta.setAlignment(Pos.CENTER);
        hboxQta.setPrefHeight(28.0);
        hboxQta.setPrefWidth(95.0); // Leggermente più largo per il testo
        hboxQta.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #FFF176; -fx-border-radius: 15; -fx-border-width: 2;");
        // Posizionamento in basso a destra
        AnchorPane.setBottomAnchor(hboxQta, 10.0);
        AnchorPane.setRightAnchor(hboxQta, 10.0);

        Label lblQta = new Label(articolo.getQuantita() + " pz. richiesti");
        lblQta.setStyle("-fx-font-size: 12px;");
        hboxQta.getChildren().add(lblQta);

        // Aggiunta figli all'AnchorPane
        anchor.getChildren().addAll(imgView, lblNome, lblPrezzo, hboxQta);

        // Se richiede patentino (logica opzionale, esempio)
        /*
        if (articolo.richiedePatentino()) {
            Label lblPatentino = new Label("Richiede Patentino");
            lblPatentino.setStyle("-fx-text-fill: #c62828; -fx-background-color: #ffebee; -fx-background-radius: 5; -fx-padding: 5 10 5 10;");
            lblPatentino.setLayoutX(95.0);
            lblPatentino.setLayoutY(62.0);
            anchor.getChildren().add(lblPatentino);
        }
        */

        return anchor;
    }

    @FXML
    public void onOrdineProntoClick() {
        try {
            if (ordineCorrente == null) return;

            // 1. Chiama il controller applicativo per aggiornare lo stato
            appController.confermaRitiroMerce(ordineCorrente.getId());

            // 2. Mostra feedback
            mostraInfo("Ordine Aggiornato", "L'ordine #" + ordineCorrente.getId() + " è pronto e il cliente è stato notificato.");

            // 3. Chiudi la finestra
            chiudiFinestra();

        } catch (DAOException e) {
            mostraErrore("Errore", "Impossibile aggiornare l'ordine: " + e.getMessage());
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