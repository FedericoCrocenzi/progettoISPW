package it.ispw.project.graphic_controller;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.PaymentException;
import it.ispw.project.validation.PagamentoValidator;
import it.ispw.project.view.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentGraphicController implements ControllerGraficoBase {

    private static final Logger LOGGER = Logger.getLogger(PaymentGraphicController.class.getName());
    private static final String METODO_CARTA_CREDITO = "CARTA_CREDITO";
    private static final String METODO_PAYPAL = "PAYPAL";
    private static final String METODO_CONTANTI_CONSEGNA = "CONTANTI_CONSEGNA";
    private static final String MAIN_VIEW_PATH = "/view/MainView.fxml";
    private static final String TITOLO_ACQUISTA_ARTICOLO = "Pagamento Effettuato";
    private static final String MESSAGGIO_ORDINE_COMPLETATO = "Ordine completato correttamente.";

    @FXML private TextField txtNumeroCarta;
    @FXML private TextField txtIntestatario;
    @FXML private TextField txtScadenzaMese;
    @FXML private TextField txtScadenzaAnno;
    @FXML private TextField txtCvv;
    @FXML private TextField txtEmailPaypal;
    @FXML private PasswordField txtPasswordPaypal;

    @FXML private RadioButton rbCarta;
    @FXML private RadioButton rbPaypal;
    @FXML private RadioButton rbContanti;
    @FXML private ToggleGroup gruppoPagamento;

    @FXML private Button btnConferma;
    @FXML private Button btnIndietro;
    @FXML private Button btnCassa;

    @FXML private Label lblTotale;
    @FXML private VBox boxDatiCarta;
    @FXML private VBox boxDatiPaypal;

    private AcquistaArticoloControllerApplicativo appController;
    private String sessionId;

    @FXML
    public void initialize() {
        if (gruppoPagamento != null) {
            gruppoPagamento.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> aggiornaCampiMetodoPagamento());
        }
        aggiornaCampiMetodoPagamento();
    }

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
        aggiornaTotaleOrdine();
        aggiornaCampiMetodoPagamento();
    }

    private void aggiornaTotaleOrdine() {
        try {
            CarrelloBean carrello = appController.visualizzaCarrello(sessionId);
            lblTotale.setText(String.format("€ %.2f", carrello.getTotale()));
        } catch (Exception e) {
            lblTotale.setText("€ 0.00");
        }
    }

    /* =========================
       PAGA ORA
       ========================= */
    @FXML
    public void confermaPagamento() {
        try {
            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            PagamentoBean pagamentoBean = creaPagamentoDaSelezione(carrelloTmp);
            PagamentoValidator.valida(pagamentoBean);

            OrdineBean ordineBean = completaOrdineConPagamento(pagamentoBean, carrelloTmp);

            Stage stage = (Stage) btnConferma.getScene().getWindow();
            mostraPopupAcquistoCompletato(ordineBean, stage);

        } catch (PaymentException e) {
            mostraErrorePagamento(e);
        } catch (DAOException e) {
            mostraErroreSistema("Errore tecnico durante il completamento dell'ordine.",
                    "Impossibile completare l'ordine. Riprova piu' tardi.", e);
        } catch (Exception e) {
            mostraErroreImprevisto("Errore imprevisto durante il pagamento.", e);
        }
    }

    /* =========================
       PAGA IN CASSA
       ========================= */
    @FXML
    public void pagaInCassa() {
        try {
            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            PagamentoBean pagamentoBean = creaPagamentoInCassa(carrelloTmp);

            completaOrdineConPagamento(pagamentoBean, carrelloTmp);
            mostraMessaggio("Pagamento in cassa", "Ti aspettiamo in cassa!", Alert.AlertType.INFORMATION);

        } catch (PaymentException e) {
            mostraErrorePagamento(e);
            return;
        } catch (DAOException e) {
            mostraErroreSistema("Errore tecnico durante la conferma del pagamento in cassa.",
                    "Impossibile confermare l'ordine. Riprova piu' tardi.", e);
            return;
        } catch (Exception e) {
            mostraErroreImprevisto("Errore imprevisto durante il pagamento in cassa.", e);
            return;
        }

        Stage stage = (Stage) btnCassa.getScene().getWindow();
        ViewSwitcher.switchTo(MAIN_VIEW_PATH, sessionId, stage);
    }

    @FXML
    public void tornaAlCarrello() {
        Stage stage = (Stage) btnIndietro.getScene().getWindow();
        ViewSwitcher.switchTo(MAIN_VIEW_PATH, sessionId, stage);
    }

    private void mostraMessaggio(String titolo, String testo, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    private PagamentoBean creaPagamentoDaSelezione(CarrelloBean carrelloTmp) {
        PagamentoBean pagamentoBean = new PagamentoBean();

        if (rbCarta.isSelected()) {
            popolaDatiCarta(pagamentoBean);
        } else if (rbPaypal.isSelected()) {
            popolaDatiPaypal(pagamentoBean);
        } else {
            pagamentoBean.setMetodoPagamento(METODO_CONTANTI_CONSEGNA);
        }

        impostaImportoDaPagare(pagamentoBean, carrelloTmp);
        return pagamentoBean;
    }

    private PagamentoBean creaPagamentoInCassa(CarrelloBean carrelloTmp) {
        PagamentoBean pagamentoBean = new PagamentoBean();
        pagamentoBean.setMetodoPagamento(METODO_CONTANTI_CONSEGNA);
        impostaImportoDaPagare(pagamentoBean, carrelloTmp);
        return pagamentoBean;
    }

    private void popolaDatiCarta(PagamentoBean pagamentoBean) {
        pagamentoBean.setMetodoPagamento(METODO_CARTA_CREDITO);
        pagamentoBean.setNumeroCarta(testoCampo(txtNumeroCarta));
        pagamentoBean.setIntestatario(testoCampo(txtIntestatario));
        pagamentoBean.setDataScadenza(testoCampo(txtScadenzaMese) + "/" + testoCampo(txtScadenzaAnno));
        pagamentoBean.setCvv(testoCampo(txtCvv));
    }

    private void popolaDatiPaypal(PagamentoBean pagamentoBean) {
        pagamentoBean.setMetodoPagamento(METODO_PAYPAL);
        pagamentoBean.setEmailPaypal(testoCampo(txtEmailPaypal));
        pagamentoBean.setPasswordPaypal(testoCampo(txtPasswordPaypal));
    }

    private void impostaImportoDaPagare(PagamentoBean pagamentoBean, CarrelloBean carrelloTmp) {
        pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());
    }

    private OrdineBean completaOrdineConPagamento(PagamentoBean pagamentoBean, CarrelloBean carrelloTmp)
            throws DAOException, PaymentException {
        OrdineBean ordineBean = appController.completaAcquisto(sessionId, pagamentoBean);
        ordineBean.setArticoli(carrelloTmp.getListaArticoli());
        ordineBean.setTotale(carrelloTmp.getTotale());
        if (!CommessoGraphicController.isCommessoGraficoAttivo()) {
            CommessoGraphicController.registraNuovoOrdineInElaborazione(ordineBean);
        }
        return ordineBean;
    }

    private void aggiornaCampiMetodoPagamento() {
        boolean cartaSelezionata = rbCarta != null && rbCarta.isSelected();
        boolean paypalSelezionato = rbPaypal != null && rbPaypal.isSelected();

        aggiornaVisibilitaSezione(boxDatiCarta, cartaSelezionata);
        aggiornaVisibilitaSezione(boxDatiPaypal, paypalSelezionato);
    }

    private void aggiornaVisibilitaSezione(VBox sezione, boolean visibile) {
        if (sezione != null) {
            sezione.setVisible(visibile);
            sezione.setManaged(visibile);
        }
    }

    private String testoCampo(TextField campo) {
        return campo == null || campo.getText() == null ? "" : campo.getText().trim();
    }

    private void mostraErrorePagamento(PaymentException e) {
        mostraMessaggio("Errore Pagamento", e.getMessage(), Alert.AlertType.WARNING);
    }

    private void mostraErroreSistema(String logMessage, String userMessage, Exception e) {
        LOGGER.log(Level.SEVERE, logMessage, e);
        mostraMessaggio("Errore Sistema", userMessage, Alert.AlertType.ERROR);
    }

    private void mostraErroreImprevisto(String logMessage, Exception e) {
        LOGGER.log(Level.SEVERE, logMessage, e);
        mostraMessaggio("Errore Imprevisto",
                "Si e' verificato un errore imprevisto. Riprova piu' tardi.",
                Alert.AlertType.ERROR);
    }

    private void mostraPopupAcquistoCompletato(OrdineBean ordineBean, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaOrdinePronto.fxml"));
            Parent root = loader.load();

            final boolean[] homeAperta = {false};
            NotificaOrdineProntoGraphicController popupController = loader.getController();
            popupController.configura(
                    ordineBean,
                    TITOLO_ACQUISTA_ARTICOLO,
                    MESSAGGIO_ORDINE_COMPLETATO,
                    "Torna alla Home",
                    "/image/pagamento_approvato.png",
                    () -> {
                        homeAperta[0] = true;
                        ViewSwitcher.switchTo(MAIN_VIEW_PATH, sessionId, owner);
                    }
            );

            Stage popupStage = new Stage();
            popupStage.initOwner(owner);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(TITOLO_ACQUISTA_ARTICOLO);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            if (!homeAperta[0]) {
                ViewSwitcher.switchTo(MAIN_VIEW_PATH, sessionId, owner);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento popup acquisto completato.", e);
            mostraMessaggio(TITOLO_ACQUISTA_ARTICOLO,
                    MESSAGGIO_ORDINE_COMPLETATO,
                    Alert.AlertType.INFORMATION);
            ViewSwitcher.switchTo(MAIN_VIEW_PATH, sessionId, owner);
        }
    }
}
