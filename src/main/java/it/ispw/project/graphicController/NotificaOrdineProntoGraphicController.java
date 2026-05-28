package it.ispw.project.graphicController;

import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.OrdineBean;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class NotificaOrdineProntoGraphicController {

    @FXML private ImageView imgIcona;
    @FXML private Label lblTitolo;
    @FXML private Label lblMessaggio;
    @FXML private Label lblOrdine;
    @FXML private Label lblTotale;
    @FXML private VBox vboxArticoli;
    @FXML private Button btnAzione;

    private Runnable azioneConferma;

    public void configura(OrdineBean ordine,
                          String titolo,
                          String messaggio,
                          String testoPulsante,
                          String iconaPath,
                          Runnable azioneConferma) {
        this.azioneConferma = azioneConferma;

        lblTitolo.setText(titolo != null ? titolo : "Notifica ordine");
        lblMessaggio.setText(messaggio != null ? messaggio : "");
        btnAzione.setText(testoPulsante != null ? testoPulsante : "Chiudi");

        caricaIcona(iconaPath);
        mostraDettagliOrdine(ordine);
    }

    @FXML
    private void onAzioneClick() {
        chiudiPopup();
        if (azioneConferma != null) {
            azioneConferma.run();
        }
    }

    @FXML
    private void onChiudiClick() {
        chiudiPopup();
    }

    private void mostraDettagliOrdine(OrdineBean ordine) {
        vboxArticoli.getChildren().clear();

        if (ordine == null) {
            lblOrdine.setText("Ordine non disponibile");
            lblTotale.setText("");
            return;
        }

        lblOrdine.setText("Ordine #" + ordine.getId() + formattaStato(ordine.getStato()));
        lblTotale.setText(String.format("Totale: EUR %.2f", ordine.getTotale()));

        List<ArticoloBean> articoli = ordine.getArticoli();
        if (articoli == null || articoli.isEmpty()) {
            vboxArticoli.getChildren().add(new Label("Dettagli articoli non disponibili."));
            return;
        }

        for (ArticoloBean articolo : articoli) {
            ImageView imgArticolo = new ImageView();
            imgArticolo.setFitHeight(34.0);
            imgArticolo.setFitWidth(34.0);
            imgArticolo.setPreserveRatio(true);
            caricaImmagineArticolo(imgArticolo, articolo.getImmaginePath());

            Label testoArticolo = new Label(formattaArticolo(articolo));
            testoArticolo.setWrapText(true);
            testoArticolo.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

            HBox rigaArticolo = new HBox(8.0, imgArticolo, testoArticolo);
            rigaArticolo.setAlignment(Pos.CENTER_LEFT);
            vboxArticoli.getChildren().add(rigaArticolo);
        }
    }

    private String formattaArticolo(ArticoloBean articolo) {
        String descrizione = articolo.getDescrizione() != null ? articolo.getDescrizione() : "Articolo";
        return String.format("%s - qta %d - EUR %.2f",
                descrizione,
                articolo.getQuantita(),
                articolo.getPrezzo());
    }

    private String formattaStato(String stato) {
        return stato == null || stato.isBlank() ? "" : " - " + stato;
    }

    private void caricaImmagineArticolo(ImageView imgView, String path) {
        String imagePath = (path == null || path.isBlank()) ? "/Image/logo1.png" : path;
        try {
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is == null) {
                is = getClass().getResourceAsStream("/Image/logo1.png");
            }
            if (is != null) {
                imgView.setImage(new Image(is));
            }
        } catch (RuntimeException e) {
            imgView.setImage(null);
        }
    }

    private void caricaIcona(String iconaPath) {
        if (iconaPath == null || iconaPath.isBlank()) {
            imgIcona.setImage(null);
            return;
        }

        try {
            imgIcona.setImage(new Image(getClass().getResourceAsStream(iconaPath)));
        } catch (RuntimeException e) {
            imgIcona.setImage(null);
        }
    }

    private void chiudiPopup() {
        Stage stage = (Stage) btnAzione.getScene().getWindow();
        stage.close();
    }
}
