package it.ispw.project.graphicController;


import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.model.Carrello;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.sessionManager.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CarrelloGraphicController implements ControllerGraficoBase, Observer {

    @FXML private ListView<String> listaArticoliView; // O TableView<ArticoloBean>
    @FXML private Label lblTotale;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId; // Manteniamo il sessionId per riferimenti futuri se necessario

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // SOLUZIONE:
        // Creiamo il controller applicativo QUI, dove abbiamo il sessionId,
        // invece che nel costruttore della classe.
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // REGISTRAZIONE OBSERVER:
        // Recupero il model Carrello dalla sessione per osservarlo
        Carrello carrelloModel = SessionManager.getInstance().getSession(sessionId).getCarrelloCorrente();
        carrelloModel.attach(this);

        // Prima visualizzazione
        aggiornaVista();
    }

    @FXML
    public void procediAlPagamento() {
        try {
            CarrelloBean carrello = appController.visualizzaCarrello();
            if (carrello.getListaArticoli().isEmpty()) {
                mostraMessaggio("Attenzione", "Il carrello è vuoto!");
                return;
            }

            // Simulazione funzionalità non implementata nella demo grafica
            // In un progetto completo, qui caricheresti "PagamentoView.fxml"
            mostraMessaggio("Pagamento", "Funzionalità di pagamento simulata.\nIndirizzamento al gateway PayPal...");

            // Qui chiameresti appController.completaAcquisto(...)

        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage());
        }
    }

    @FXML
    public void svuotaCarrelloClick() {
        // Simuliamo un'azione
        mostraMessaggio("Info", "Funzionalità di svuotamento rapido non ancora collegata al backend.");
    }

    // --- Metodi Observer ---
    @Override
    public void update(Object subject) {
        // Quando il model notifica, aggiorniamo la GUI
        aggiornaVista();
    }

    private void aggiornaVista() {
        CarrelloBean bean = appController.visualizzaCarrello();

        listaArticoliView.getItems().clear();
        for (ArticoloBean art : bean.getListaArticoli()) {
            String riga = art.getDescrizione() + " - Qta: " + art.getQuantita() + " - € " + art.getPrezzo();
            listaArticoliView.getItems().add(riga);
        }

        lblTotale.setText(String.format("Totale: € %.2f", bean.getTotale()));
    }

    private void mostraMessaggio(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}