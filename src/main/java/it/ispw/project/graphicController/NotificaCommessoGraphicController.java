package it.ispw.project.graphicController;

import it.ispw.project.bean.OrdineBean;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class NotificaCommessoGraphicController {

    @FXML private Label lblTitolo;
    @FXML private Label lblOrdine;
    @FXML private Button btnVisualizza;

    private OrdineBean ordineCorrente;
    private CommessoGraphicController parentController;

    /**
     * Inizializza i dati del popup.
     * @param ordine L'ordine da notificare (Bean).
     * @param parent Il controller principale che ha aperto il popup.
     */
    public void initData(OrdineBean ordine, CommessoGraphicController parent) {
        this.ordineCorrente = ordine;
        this.parentController = parent;

        if (ordine != null) {
            lblTitolo.setText("Nuovo Ordine!");
            lblOrdine.setText("È arrivato l'ordine #" + ordine.getId());
        } else {
            // Caso messaggio generico (es. Cliente in negozio)
            lblTitolo.setText("Avviso");
            lblOrdine.setText("Nuova notifica generica.");
            btnVisualizza.setText("Chiudi");
        }
    }

    /**
     * Metodo per messaggi puramente testuali (es. "Cliente in negozio")
     */
    public void setMessaggioSemplice(String titolo, String messaggio) {
        lblTitolo.setText(titolo);
        lblOrdine.setText(messaggio);
        btnVisualizza.setText("Chiudi");
        this.ordineCorrente = null; // Nessun ordine da aprire
    }

    @FXML
    public void onVisualizzaClick() {
        // Chiude il popup corrente
        Stage stage = (Stage) btnVisualizza.getScene().getWindow();
        stage.close();

        // Se c'è un ordine associato, dice al padre di aprire i dettagli
        if (ordineCorrente != null && parentController != null) {
            parentController.apriDettaglioOrdine(ordineCorrente);
        }
    }
}