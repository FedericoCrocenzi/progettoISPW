package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.PagamentoBean;
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
        // CORREZIONE 1: Costruttore vuoto (Stateless)
        this.appController = new AcquistaArticoloControllerApplicativo();
    }

    @FXML
    public void confermaPagamento() {
        try {
            PagamentoBean pagamentoBean = new PagamentoBean();

            // 1. Logica popolamento bean
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
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
            }

            // CORREZIONE 2: Passaggio di sessionId per visualizzare il totale corretto
            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());

            // CORREZIONE 3: Nuova firma (sessionId, pagamentoBean).
            // Non passiamo più carrelloBean o UtenteBean dal client, li recupera il controller dalla sessione.
            appController.completaAcquisto(sessionId, pagamentoBean);

            // 3. Navigazione verso NOTIFICA
            Stage stage = (Stage) btnConferma.getScene().getWindow();
            ViewSwitcher.switchTo("/view/notificaPagamentoEffettuttoView.fxml", sessionId, stage);

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
        ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, stage);
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }
}