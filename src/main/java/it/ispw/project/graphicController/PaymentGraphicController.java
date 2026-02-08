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
import javafx.scene.control.Label;
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
    @FXML private Button btnCassa;      // <<< AGGIUNTO

    @FXML private Label lblTotale;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        aggiornaTotaleOrdine();
    }

    private void aggiornaTotaleOrdine() {
        if (lblTotale == null) return;

        try {
            CarrelloBean carrello = appController.visualizzaCarrello(sessionId);
            lblTotale.setText(String.format("€ %.2f", carrello.getTotale()));
        } catch (Exception e) {
            lblTotale.setText("€ 0.00");
        }
    }

    /* =========================
       PAGA ORA (pagamento classico)
       ========================= */
    @FXML
    public void confermaPagamento() {
        try {
            PagamentoBean pagamentoBean = new PagamentoBean();

            if (rbCarta != null && rbCarta.isSelected()) {
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
                pagamentoBean.setNumeroCarta(txtNumeroCarta.getText());
                pagamentoBean.setIntestatario(txtIntestatario.getText());
                pagamentoBean.setDataScadenza(
                        txtScadenzaMese.getText() + "/" + txtScadenzaAnno.getText()
                );
                pagamentoBean.setCvv(txtCvv.getText());
            } else if (rbPaypal != null && rbPaypal.isSelected()) {
                pagamentoBean.setMetodoPagamento("PAYPAL");
            } else if (rbContanti != null && rbContanti.isSelected()) {
                pagamentoBean.setMetodoPagamento("CONTANTI_CONSEGNA");
            } else {
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
            }

            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());

            appController.completaAcquisto(sessionId, pagamentoBean);

            Stage stage = (Stage) btnConferma.getScene().getWindow();
            ViewSwitcher.switchTo("/view/notificaPagamentoEffettuatoView.fxml", sessionId, stage);

        } catch (PaymentException e) {
            mostraMessaggio("Errore Pagamento", e.getMessage(), Alert.AlertType.WARNING);
        } catch (DAOException e) {
            mostraMessaggio(
                    "Errore Sistema",
                    "Impossibile completare l'ordine: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        } catch (Exception e) {
            mostraMessaggio("Errore Imprevisto", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /* =========================
       PAGA IN CASSA
       ========================= */
    @FXML
    public void pagaInCassa() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pagamento in cassa");
        alert.setHeaderText(null);
        alert.setContentText("Ti aspettiamo in cassa!");
        alert.showAndWait();

        Stage stage = (Stage) btnCassa.getScene().getWindow();
        ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, stage);
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