package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.NotificaOrdineBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.RicercaArticoloBean; // Assicurati che questo import esista
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.view.ViewSwitcher; // Se usi ViewSwitcher per il logout, altrimenti lascia stare
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainGraphicController implements ControllerGraficoBase, Observer {

    private static final Logger LOGGER = Logger.getLogger(MainGraphicController.class.getName());
    private static boolean clienteGraficoAttivo;
    private static MainGraphicController controllerClienteRegistrato;

    @FXML private BorderPane rootLayout; // Il contenitore principale
    @FXML private ToggleButton btnHome;
    @FXML private ToggleButton btnCarrello;
    @FXML private ToggleButton btnProfilo;
    @FXML private ToggleGroup menuGroup;

    // --- NUOVO CAMPO PER LA RICERCA ---
    @FXML private TextField txtRicerca;

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        setClienteGraficoAttivo(true);
        MainGraphicController precedente = sostituisciControllerCliente(this);
        if (precedente != null && precedente != this) {
            GestoreNotifiche.getInstance().detach(precedente);
        }
        GestoreNotifiche.getInstance().attach(this);
        // All'avvio carica la Home (Catalogo completo)
        mostraHome();
        Platform.runLater(this::mostraNotificheMerceProntaInAttesa);
    }

    /**
     * Metodo chiamato dal bottone lente o premendo Invio nella TextField.
     * Crea il bean di ricerca e ricarica il catalogo filtrato.
     */
    @FXML
    public void cercaProdotti() {
        String testo = txtRicerca.getText();

        // 1. Creazione del Bean per il trasferimento dati (Pattern Bean)
        RicercaArticoloBean beanRicerca = new RicercaArticoloBean();
        beanRicerca.setTestoRicerca(testo);

        // 2. Caricamento manuale del Catalogo per passare il filtro
        try {
            // Nota: percorso corretto "/view/..." (senza /main/resources se lanci da IDE compilato)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CatalogoView.fxml"));
            Node vista = loader.load();

            // 3. Recupero del controller e passaggio dati specifici
            CatalogoGraphicController catController = loader.getController();
            // Chiama il metodo overloaded che accetta il filtro (definito nel passaggio precedente)
            catController.initData(sessionId, beanRicerca);

            // 4. Aggiornamento vista centrale
            rootLayout.setCenter(vista);

            // Mantiene il bottone Home selezionato perché siamo tecnicamente nel catalogo
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

            // Inizializza il controller della sotto-vista se necessario
            Object controller = loader.getController();
            if (controller instanceof ControllerGraficoBase) {
                ((ControllerGraficoBase) controller).initData(sessionId);
            }

            // Sostituisce il centro del BorderPane
            rootLayout.setCenter(vista);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento vista: " + fxmlPath, e);
        }
    }

    @FXML
    public void mostraHome() {
        // Pulisce la barra di ricerca quando si torna alla home "pulita"
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
        // Assicurati che il file si chiami ProfileView.fxml o profileView.fxml (case sensitive)
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

    public static synchronized boolean isClienteGraficoAttivo() {
        return clienteGraficoAttivo;
    }

    private static synchronized void setClienteGraficoAttivo(boolean attivo) {
        clienteGraficoAttivo = attivo;
    }

    private static synchronized MainGraphicController sostituisciControllerCliente(MainGraphicController controller) {
        MainGraphicController precedente = controllerClienteRegistrato;
        controllerClienteRegistrato = controller;
        return precedente;
    }

    private static synchronized void rimuoviControllerCliente(MainGraphicController controller) {
        if (controllerClienteRegistrato == controller) {
            controllerClienteRegistrato = null;
            clienteGraficoAttivo = false;
        }
    }

    @Override
    public void update(Object data) {
        if (!(data instanceof NotificaOrdineBean)) {
            return;
        }

        NotificaOrdineBean notifica = (NotificaOrdineBean) data;

        Platform.runLater(() -> {
            if (!isSchermataClienteAttiva()) {
                rimuoviControllerCliente(this);
                GestoreNotifiche.getInstance().detach(this);
                return;
            }

            if (appController.notificaDestinataAllaSessione(sessionId, notifica)) {
                mostraPopupMercePronta(notifica);
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
                    "/Image/icons-logistica.png",
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
