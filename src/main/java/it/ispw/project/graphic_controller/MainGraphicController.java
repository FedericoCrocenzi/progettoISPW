package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.NotificaOrdineBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.RicercaArticoloBean;
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.NotificaOrdine;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainGraphicController implements ControllerGraficoBase, Observer {

    private static final Logger LOGGER = Logger.getLogger(MainGraphicController.class.getName());
    private static MainGraphicController controllerClienteRegistrato;

    @FXML private BorderPane rootLayout;
    @FXML private ToggleButton btnHome;
    @FXML private ToggleButton btnCarrello;
    @FXML private ToggleButton btnProfilo;
    @FXML private TextField txtRicerca;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        MainGraphicController precedente = sostituisciControllerCliente(this);
        if (precedente != null && precedente != this) {
            GestoreNotifiche.getInstance().detach(precedente);
        }
        GestoreNotifiche.getInstance().attach(this);
        mostraHome();
        Platform.runLater(this::mostraNotificheMerceProntaInAttesa);
    }

    /**
     Metodo chiamato dal bottone lente o premendo Invio nella TextField.
     */
    @FXML
    public void cercaProdotti() {
        String testo = txtRicerca.getText();

        RicercaArticoloBean beanRicerca = new RicercaArticoloBean();
        beanRicerca.setTestoRicerca(testo);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CatalogoView.fxml"));
            Node vista = loader.load();

            CatalogoGraphicController catController = loader.getController();
            catController.initData(sessionId, beanRicerca);

            rootLayout.setCenter(vista);

            if (btnHome != null) btnHome.setSelected(true);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca prodotti.", e);
        }
    }

    /**
     * Metodo generico per cambiare la vista centrale mantenendo Top/Bottom bar.
     */
    private void caricaVistaCentrale(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase controllerGraficoBase) {
                controllerGraficoBase.initData(sessionId);
            }

            rootLayout.setCenter(vista);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e,
                    () -> MessageFormat.format("Errore caricamento vista: {0}", fxmlPath));
        }
    }

    @FXML
    public void mostraHome() {
        if (txtRicerca != null) {
            txtRicerca.setText("");
        }
        caricaVistaCentrale("/view/CatalogoView.fxml");
        if (btnHome != null) btnHome.setSelected(true);
    }

    @FXML
    public void mostraCarrello() {
        caricaVistaCentrale("/view/carrelloView.fxml");
        if (btnCarrello != null) btnCarrello.setSelected(true);
    }

    @FXML
    public void mostraProfilo() {
        caricaVistaCentrale("/view/profileView.fxml");
        if (btnProfilo != null) btnProfilo.setSelected(true);
    }

    @FXML
    public void scannerizzaBarcode() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Scannerizza Barcode");
        alert.setHeaderText(null);
        alert.setContentText("Funzionalità non ancora implementata.");
        alert.showAndWait();
    }

    @FXML
    public void logout() {
        rimuoviControllerCliente(this);
        GestoreNotifiche.getInstance().detach(this);
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        ViewSwitcher.switchTo("/view/Login.fxml", null, stage);
    }

    private static synchronized MainGraphicController sostituisciControllerCliente(MainGraphicController controller) {
        MainGraphicController precedente = controllerClienteRegistrato;
        controllerClienteRegistrato = controller;
        return precedente;
    }

    private static synchronized void rimuoviControllerCliente(MainGraphicController controller) {
        if (controllerClienteRegistrato == controller) {
            controllerClienteRegistrato = null;
        }
    }

    @Override
    public void update(Object data) {
        if (!(data instanceof NotificaOrdine)) {
            return;
        }

        NotificaOrdine notifica = (NotificaOrdine) data;
        if (notifica.getTipo() != NotificaOrdine.Tipo.MERCE_PRONTA) {
            return;
        }

        Platform.runLater(() -> {
            if (!isSchermataClienteAttiva()) {
                rimuoviControllerCliente(this);
                GestoreNotifiche.getInstance().detach(this);
                return;
            }

            if (appController.notificaDestinataAllaSessione(sessionId, notifica)) {
                NotificaOrdineBean notificaBean = appController.convertiNotificaOrdineInBean(notifica);
                if (notificaBean != null) {
                    mostraPopupMercePronta(notificaBean);
                }
            }
        });
    }

    private boolean isSchermataClienteAttiva() {
        return rootLayout != null
                && rootLayout.getScene() != null
                && rootLayout.getScene().getWindow() != null
                && rootLayout.getScene().getWindow().isShowing();
    }

    private void mostraNotificheMerceProntaInAttesa() {
        if (!isSchermataClienteAttiva()) {
            return;
        }

        for (NotificaOrdineBean notifica : appController.recuperaNotificheMercePronta(sessionId)) {
            mostraPopupMercePronta(notifica);
        }
    }

    private void mostraPopupMercePronta(NotificaOrdineBean notifica) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaOrdinePronto.fxml"));
            Parent root = loader.load();

            OrdineBean ordineBean = notifica.getOrdine();
            if (ordineBean == null) {
                ordineBean = new OrdineBean();
                ordineBean.setId(notifica.getIdOrdine());
                ordineBean.setStato(notifica.getStato());
            }

            NotificaOrdineProntoGraphicController popupController = loader.getController();
            popupController.configura(
                    ordineBean,
                    "Merce Pronta",
                    "Il tuo ordine e' pronto per il ritiro.",
                    "Sono Qui",
                    "/image/order-purchase.png",
                    () -> appController.confermaLetturaNotificaMercePronta(notifica.getIdOrdine())
            );

            Stage popupStage = new Stage();
            popupStage.initOwner(rootLayout.getScene().getWindow());
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Merce Pronta");
            popupStage.setScene(new Scene(root));
            popupStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento popup Merce Pronta.", e);
        }
    }
}
