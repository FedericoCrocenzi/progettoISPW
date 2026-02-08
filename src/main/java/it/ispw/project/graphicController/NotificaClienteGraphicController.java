package it.ispw.project.graphicController;

import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.observer.Observer;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * ConcreteObserver [cite: 1614] che gestisce la visualizzazione della notifica di pagamento per il cliente.
 * Si iscrive al GestoreNotifiche (Subject) per coerenza architetturale.
 */
public class NotificaClienteGraphicController implements ControllerGraficoBase, Observer {

    @FXML private Label lblOrdine;
    @FXML private Button btnHome;

    private String sessionId;
    private int idOrdineCorrente;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;

        // 1. Registrazione come Observer (Attach)
        GestoreNotifiche.getInstance().attach(this);

        // 2. Recupero dati iniziali dalla sessione (Stato iniziale)
        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session != null) {
            Ordine ordine = session.getUltimoOrdineCreato();
            if (ordine != null) {
                this.idOrdineCorrente = ordine.leggiId();
                aggiornaTesto("Ordine n° " + idOrdineCorrente + " confermato con successo!");
            }
        }
    }

    /**
     * Metodo dell'interfaccia Observer.
     * Viene chiamato quando il Subject (GestoreNotifiche) notifica un cambiamento.
     */
    @Override
    public void update(Object data) {
        // Uso Platform.runLater perché le notifiche potrebbero arrivare da thread diversi
        // e JavaFX richiede che la GUI sia aggiornata dal thread applicativo.
        Platform.runLater(() -> {
            if (data instanceof Ordine) {
                // Se riceviamo un oggetto Ordine (es. aggiornamento stato), controlliamo se è il nostro
                Ordine o = (Ordine) data;
                if (o.leggiId() == this.idOrdineCorrente) {
                    aggiornaTesto("Stato Ordine #" + o.leggiId() + ": " + o.getStato());
                }
            } else if (data instanceof String) {
                // Se riceviamo un messaggio testuale
                aggiornaTesto((String) data);
            }
        });
    }

    private void aggiornaTesto(String messaggio) {
        if (lblOrdine != null) {
            lblOrdine.setText(messaggio);
        }
    }

    @FXML
    public void tornaAllaHome() {
        // 3. Deregistrazione (Detach) prima di chiudere
        GestoreNotifiche.getInstance().detach(this);

        // 4. Navigazione verso la Home (MainView)
        Stage stage = (Stage) btnHome.getScene().getWindow();
        ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, stage);
    }
}