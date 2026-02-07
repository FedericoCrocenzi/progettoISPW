package it.ispw.project.graphicController;

import it.ispw.project.bean.RicercaArticoloBean; // Assicurati che questo import esista
import it.ispw.project.view.ViewSwitcher; // Se usi ViewSwitcher per il logout, altrimenti lascia stare
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainGraphicController implements ControllerGraficoBase {

    @FXML private BorderPane rootLayout; // Il contenitore principale
    @FXML private ToggleButton btnHome;
    @FXML private ToggleButton btnCarrello;
    @FXML private ToggleButton btnProfilo;
    @FXML private ToggleGroup menuGroup;

    // --- NUOVO CAMPO PER LA RICERCA ---
    @FXML private TextField txtRicerca;

    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        // All'avvio carica la Home (Catalogo completo)
        mostraHome();
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
            e.printStackTrace();
            System.err.println("Errore durante la ricerca prodotti: " + e.getMessage());
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
            e.printStackTrace();
            System.err.println("Errore caricamento vista: " + fxmlPath);
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
        caricaVistaCentrale("/view/CarrelloView.fxml");
        if (btnCarrello != null) btnCarrello.setSelected(true);
    }

    @FXML
    public void mostraProfilo() {
        // Assicurati che il file si chiami ProfileView.fxml o profileView.fxml (case sensitive)
        caricaVistaCentrale("/view/profileView.fxml");
        if (btnProfilo != null) btnProfilo.setSelected(true);
    }

    @FXML
    public void logout() {
        // Esempio di logout: chiude la finestra attuale e riapre il Login
        // Assumiamo che tu abbia una classe ViewSwitcher o simile
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) rootLayout.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}