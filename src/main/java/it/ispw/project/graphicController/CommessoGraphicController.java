package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommessoGraphicController implements ControllerGraficoBase, Observer {

    private static final Logger LOGGER = Logger.getLogger(CommessoGraphicController.class.getName());
    private static final Map<Integer, OrdineBean> nuoviOrdiniInAttesa = new LinkedHashMap<>();
    private static boolean commessoGraficoAttivo;

    @FXML private TilePane tilePaneOrdini;
    @FXML private ToggleGroup menuGroup;
    @FXML private Label lblNomeUtente;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;
    private boolean popupLoginMostrato;

    @FXML
    public void initialize() {
        if (menuGroup != null) {
            menuGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    ToggleButton selected = (ToggleButton) newVal;
                    if ("Profilo".equals(selected.getText())) {
                        mostraInfo("Funzionalita Non Disponibile", "La schermata Profilo non e' stata ancora implementata.");
                    }
                }
            });
        }
    }

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();

        setCommessoGraficoAttivo(true);
        GestoreNotifiche.getInstance().attach(this);

        List<OrdineBean> ordiniCorrenti = caricaOrdini();
        Platform.runLater(() -> {
            boolean notificheMostrate = mostraNuoviOrdiniInAttesa();
            if (!notificheMostrate) {
                mostraPopupLoginSeNecessario(ordiniCorrenti);
            }
        });
    }

    /**
     * Deregistra l'observer grafico quando il commesso esce dalla schermata.
     */
    public void onClose() {
        setCommessoGraficoAttivo(false);
        GestoreNotifiche.getInstance().detach(this);
    }

    public static synchronized boolean isCommessoGraficoAttivo() {
        return commessoGraficoAttivo;
    }

    public static synchronized void registraNuovoOrdineInAttesa(OrdineBean ordineBean) {
        if (ordineBean != null && ordineBean.getId() > 0 && "IN_ATTESA".equals(ordineBean.getStato())) {
            nuoviOrdiniInAttesa.putIfAbsent(ordineBean.getId(), ordineBean);
        }
    }

    private static synchronized void rimuoviNuovoOrdineInAttesa(int idOrdine) {
        nuoviOrdiniInAttesa.remove(idOrdine);
    }

    private static synchronized void setCommessoGraficoAttivo(boolean attivo) {
        commessoGraficoAttivo = attivo;
    }

    private static synchronized List<OrdineBean> prelevaNuoviOrdiniInAttesa() {
        List<OrdineBean> ordini = new ArrayList<>(nuoviOrdiniInAttesa.values());
        nuoviOrdiniInAttesa.clear();
        return ordini;
    }

    public List<OrdineBean> caricaOrdini() {
        if (tilePaneOrdini == null) return new ArrayList<>();

        try {
            tilePaneOrdini.getChildren().clear();
            List<OrdineBean> ordini = appController.recuperaOrdiniPendenti();

            if (ordini.isEmpty()) {
                tilePaneOrdini.getChildren().add(new Label("Nessun ordine da evadere."));
                return ordini;
            }

            for (OrdineBean ordine : ordini) {
                aggiungiCardOrdine(ordine);
            }

            return ordini;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento ordini pendenti.", e);
            mostraErrore("Errore Sistema", "Impossibile caricare gli ordini. Riprova piu' tardi.");
            return new ArrayList<>();
        }
    }

    private void aggiungiCardOrdine(OrdineBean ordine) {
        VBox card = new VBox(5);
        card.setPrefSize(200, 250);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
        card.setPadding(new Insets(10));

        Label lblId = new Label("Ordine n. " + ordine.getId());
        lblId.setStyle("-fx-font-size: 14px;");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(80);
        imgView.setFitWidth(80);
        imgView.setPreserveRatio(true);
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/Image/icons-logistica.png")));
        } catch (RuntimeException e) {
            imgView.setImage(null);
        }

        Label lblPrezzo = new Label(String.format("Totale: EUR %.2f", ordine.getTotale()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnDettagli = new Button("Dettagli");
        btnDettagli.getStyleClass().add("bottone-giallo");
        btnDettagli.setOnAction(e -> mostraDettagli(ordine));

        Button btnPronto = new Button("Merce Pronta");
        btnPronto.setStyle("-fx-background-color: #125332; -fx-text-fill: white; -fx-cursor: hand;");
        btnPronto.setOnAction(e -> gestisciOrdinePronto(ordine.getId(), card));

        card.getChildren().addAll(lblId, imgView, lblPrezzo, btnDettagli, btnPronto);
        tilePaneOrdini.getChildren().add(card);
    }

    private void gestisciOrdinePronto(int idOrdine, VBox cardGrafica) {
        try {
            appController.confermaRitiroMerce(idOrdine);
            rimuoviNuovoOrdineInAttesa(idOrdine);
            tilePaneOrdini.getChildren().remove(cardGrafica);
            if (tilePaneOrdini.getChildren().isEmpty()) {
                tilePaneOrdini.getChildren().add(new Label("Nessun ordine da evadere."));
            }
            mostraInfo("Successo", "Stato aggiornato e cliente notificato.");
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento stato ordine.", e);
            mostraErrore("Errore", "Impossibile aggiornare l'ordine. Riprova piu' tardi.");
        }
    }

    public void apriDettaglioOrdine(OrdineBean ordine) {
        mostraDettagli(ordine);
    }

    private void mostraDettagli(OrdineBean ordine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordineCommessoView.fxml"));
            Parent root = loader.load();

            OrdineCommessoGraphicController controller = loader.getController();
            controller.initData(ordine, this);

            Stage stage = new Stage();
            if (tilePaneOrdini.getScene() != null) {
                stage.initOwner(tilePaneOrdini.getScene().getWindow());
            }
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Dettaglio Ordine");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            mostraErrore("Errore", "Impossibile aprire il dettaglio ordine.");
        }
    }

    private void apriPopupNotifica(OrdineBean ordineBean, String titolo, String messaggio) {
        Runnable azione = ordineBean != null ? () -> apriDettaglioOrdine(ordineBean) : null;
        String testoPulsante = ordineBean != null ? "Visualizza Ordine" : "Chiudi";
        String testoMessaggio = messaggio != null ? messaggio : "E' arrivato un nuovo ordine da preparare.";
        apriPopupNotifica(ordineBean, titolo, testoMessaggio, testoPulsante, "/Image/order-purchase.png", azione);
    }

    private void apriPopupNotifica(OrdineBean ordineBean,
                                   String titolo,
                                   String messaggio,
                                   String testoPulsante,
                                   String iconaPath,
                                   Runnable azione) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaOrdinePronto.fxml"));
            Parent root = loader.load();

            NotificaOrdineProntoGraphicController popupController = loader.getController();
            popupController.configura(ordineBean, titolo, messaggio, testoPulsante, iconaPath, azione);

            Stage stage = new Stage();
            if (tilePaneOrdini.getScene() != null) {
                stage.initOwner(tilePaneOrdini.getScene().getWindow());
            }
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titolo);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento popup notifica commesso.", e);
            mostraInfo(titolo, messaggio);
        }
    }

    private void mostraPopupLoginSeNecessario(List<OrdineBean> ordini) {
        if (popupLoginMostrato || ordini == null || ordini.isEmpty()) {
            return;
        }

        popupLoginMostrato = true;
        OrdineBean ordine = ordini.get(0);
        apriPopupNotifica(
                ordine,
                "Nuovo Ordine!",
                "Ordine pendente da preparare.",
                "Visualizza Ordine",
                "/Image/order-purchase.png",
                () -> apriDettaglioOrdine(ordine)
        );
    }

    private boolean mostraNuoviOrdiniInAttesa() {
        List<OrdineBean> ordini = prelevaNuoviOrdiniInAttesa();
        if (ordini.isEmpty()) {
            return false;
        }

        popupLoginMostrato = true;
        for (OrdineBean ordine : ordini) {
            apriPopupNotifica(
                    ordine,
                    "Nuovo Ordine!",
                    "E' arrivato un nuovo ordine con la lista articoli completa.",
                    "Visualizza Ordine",
                    "/Image/order-purchase.png",
                    () -> apriDettaglioOrdine(ordine)
            );
        }
        return true;
    }

    @Override
    public void update(Object data) {
        Platform.runLater(() -> {
            if (data instanceof OrdineBean) {
                OrdineBean bean = (OrdineBean) data;
                if (!"IN_ATTESA".equals(bean.getStato())) {
                    return;
                }

                caricaOrdini();

                apriPopupNotifica(
                        bean,
                        "Nuovo Ordine!",
                        "E' arrivato un nuovo ordine con la lista articoli completa.",
                        "Visualizza Ordine",
                        "/Image/order-purchase.png",
                        () -> apriDettaglioOrdine(bean)
                );

            } else if (data instanceof String) {
                String msg = (String) data;
                if (msg.contains("CLIENTE_IN_NEGOZIO")) {
                    caricaOrdini();
                    apriPopupNotifica(null, "Cliente Arrivato", msg);
                }
            }
        });
    }

    @FXML
    public void onLogoutClick() {
        onClose();
        Stage stage = (Stage) tilePaneOrdini.getScene().getWindow();
        ViewSwitcher.switchTo("/view/Login.fxml", null, stage);
    }

    private void mostraErrore(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setContentText(testo);
        alert.show();
    }

    private void mostraInfo(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setContentText(testo);
        alert.show();
    }
}
