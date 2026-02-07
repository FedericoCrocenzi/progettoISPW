package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.PaymentException;
import it.ispw.project.view.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class PaymentGraphicController implements ControllerGraficoBase {

    @FXML private TextField txtNumeroCarta;
    @FXML private TextField txtIntestatario;
    @FXML private TextField txtScadenzaMese;
    @FXML private TextField txtScadenzaAnno;
    @FXML private TextField txtCvv;

    @FXML private RadioButton rbCarta;
    @FXML private RadioButton rbPaypal;
    @FXML private RadioButton rbContanti;
    @FXML private ToggleGroup gruppoPagamento;

    @FXML private Button btnConferma;
    @FXML private Button btnIndietro;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);
    }

    @FXML
    public void confermaPagamentoClick() {
        try {
            PagamentoBean pagamentoBean = new PagamentoBean();

            // 1. Gestione Metodo Pagamento e Popolamento Bean
            if (rbCarta != null && rbCarta.isSelected()) {
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
                pagamentoBean.setNumeroCarta(txtNumeroCarta.getText());
                pagamentoBean.setIntestatario(txtIntestatario.getText());
                pagamentoBean.setDataScadenza(txtScadenzaMese.getText() + "/" + txtScadenzaAnno.getText());
                pagamentoBean.setCvv(txtCvv.getText());
            } else if (rbPaypal != null && rbPaypal.isSelected()) {
                pagamentoBean.setMetodoPagamento("PAYPAL");
            } else if (rbContanti != null && rbContanti.isSelected()) {
                pagamentoBean.setMetodoPagamento("CONTANTI_CONSEGNA");
            } else {
                // Fallback default
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
                pagamentoBean.setNumeroCarta(txtNumeroCarta.getText());
                pagamentoBean.setIntestatario(txtIntestatario.getText());
                pagamentoBean.setDataScadenza(txtScadenzaMese.getText() + "/" + txtScadenzaAnno.getText());
                pagamentoBean.setCvv(txtCvv.getText());
            }

            // 2. Impostiamo l'importo
            CarrelloBean carrelloTmp = appController.visualizzaCarrello();
            pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());

            // 3. Chiamata al Controller Applicativo
            // Nota: L'utenteBean è vuoto perché il controller usa il SessionManager per sicurezza.
            OrdineBean ordineConfermato = appController.completaAcquisto(pagamentoBean, carrelloTmp, new UtenteBean());

            // 4. Feedback Utente
            mostraMessaggio("Pagamento Riuscito",
                    "Il tuo ordine #" + ordineConfermato.getId() + " è stato confermato!",
                    Alert.AlertType.INFORMATION);

            // 5. Cambio Scena -> Verso la Notifica
            goToNotifica();

        } catch (PaymentException e) {
            mostraMessaggio("Errore Pagamento", e.getMessage(), Alert.AlertType.WARNING);
        } catch (DAOException e) {
            mostraMessaggio("Errore Sistema", "Impossibile completare l'ordine: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            mostraMessaggio("Errore Imprevisto", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void tornaAlCarrello() {
        Stage stage = (Stage) btnIndietro.getScene().getWindow();
        ViewSwitcher.switchTo("carrelloView.fxml", sessionId, stage);
    }

    // --- NUOVO METODO DI NAVIGAZIONE ---
    private void goToNotifica() {
        Stage stage = (Stage) btnConferma.getScene().getWindow();
        // Passiamo alla view di notifica/pickup.
        // L'ordine è già stato salvato in sessione dal Controller Applicativo.
        ViewSwitcher.switchTo("notificaView.fxml", sessionId, stage);
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}