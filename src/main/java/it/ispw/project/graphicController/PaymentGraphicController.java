package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.PaymentException;
import it.ispw.project.view.ViewSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.YearMonth;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentGraphicController implements ControllerGraficoBase {

    private static final Logger LOGGER = Logger.getLogger(PaymentGraphicController.class.getName());

    @FXML private TextField txtNumeroCarta;
    @FXML private TextField txtIntestatario;
    @FXML private TextField txtScadenzaMese;
    @FXML private TextField txtScadenzaAnno;
    @FXML private TextField txtCvv;
    @FXML private TextField txtEmailPaypal;
    @FXML private TextField txtConfermaEmailPaypal;

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
            PagamentoBean pagamentoBean = new PagamentoBean();

            if (rbCarta.isSelected()) {
                pagamentoBean.setMetodoPagamento("CARTA_CREDITO");
                pagamentoBean.setNumeroCarta(testoCampo(txtNumeroCarta));
                pagamentoBean.setIntestatario(testoCampo(txtIntestatario));
                pagamentoBean.setDataScadenza(
                        testoCampo(txtScadenzaMese) + "/" + testoCampo(txtScadenzaAnno)
                );
                pagamentoBean.setCvv(testoCampo(txtCvv));
            } else if (rbPaypal.isSelected()) {
                pagamentoBean.setMetodoPagamento("PAYPAL");
                pagamentoBean.setEmailPaypal(testoCampo(txtEmailPaypal));
                pagamentoBean.setConfermaEmailPaypal(testoCampo(txtConfermaEmailPaypal));
            } else {
                pagamentoBean.setMetodoPagamento("CONTANTI_CONSEGNA");
            }

            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());
            validaPagamentoInput(pagamentoBean);

            OrdineBean ordineBean = completaOrdineConPagamento(pagamentoBean, carrelloTmp);

            Stage stage = (Stage) btnConferma.getScene().getWindow();
            mostraPopupAcquistoCompletato(ordineBean, stage);

        } catch (PaymentException e) {
            mostraMessaggio("Errore Pagamento", e.getMessage(), Alert.AlertType.WARNING);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore tecnico durante il completamento dell'ordine.", e);
            mostraMessaggio("Errore Sistema",
                    "Impossibile completare l'ordine. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante il pagamento.", e);
            mostraMessaggio("Errore Imprevisto",
                    "Si e' verificato un errore imprevisto. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
        }
    }

    /* =========================
       PAGA IN CASSA
       ========================= */
    @FXML
    public void pagaInCassa() {
        try {
            CarrelloBean carrelloTmp = appController.visualizzaCarrello(sessionId);
            PagamentoBean pagamentoBean = new PagamentoBean();
            pagamentoBean.setMetodoPagamento("CONTANTI_CONSEGNA");
            pagamentoBean.setImportoDaPagare(carrelloTmp.getTotale());

            completaOrdineConPagamento(pagamentoBean, carrelloTmp);
            mostraMessaggio("Pagamento in cassa", "Ti aspettiamo in cassa!", Alert.AlertType.INFORMATION);

        } catch (PaymentException e) {
            mostraMessaggio("Errore Pagamento", e.getMessage(), Alert.AlertType.WARNING);
            return;
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore tecnico durante la conferma del pagamento in cassa.", e);
            mostraMessaggio("Errore Sistema",
                    "Impossibile confermare l'ordine. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
            return;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore imprevisto durante il pagamento in cassa.", e);
            mostraMessaggio("Errore Imprevisto",
                    "Si e' verificato un errore imprevisto. Riprova piu' tardi.",
                    Alert.AlertType.ERROR);
            return;
        }

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

    private OrdineBean completaOrdineConPagamento(PagamentoBean pagamentoBean, CarrelloBean carrelloTmp)
            throws DAOException, PaymentException {
        OrdineBean ordineBean = appController.completaAcquisto(sessionId, pagamentoBean);
        ordineBean.setArticoli(carrelloTmp.getListaArticoli());
        ordineBean.setTotale(carrelloTmp.getTotale());
        if (!CommessoGraphicController.isCommessoGraficoAttivo()) {
            CommessoGraphicController.registraNuovoOrdineInAttesa(ordineBean);
        }
        return ordineBean;
    }

    private void validaPagamentoInput(PagamentoBean pagamentoBean) throws PaymentException {
        if (pagamentoBean.getMetodoPagamento() == null || pagamentoBean.getMetodoPagamento().isBlank()) {
            throw new PaymentException("Seleziona un metodo di pagamento.");
        }

        String metodo = pagamentoBean.getMetodoPagamento();

        if ("CONTANTI_CONSEGNA".equals(metodo)) {
            return;
        }

        if ("PAYPAL".equals(metodo)) {
            validaDatiPaypal(pagamentoBean);
            return;
        }

        if ("CARTA_CREDITO".equals(metodo)) {
            validaDatiCarta(pagamentoBean);
            return;
        }

        throw new PaymentException("Metodo di pagamento non valido.");
    }

    private void validaDatiCarta(PagamentoBean pagamentoBean) throws PaymentException {
        if (isBlank(pagamentoBean.getIntestatario())
                || isBlank(pagamentoBean.getNumeroCarta())
                || isBlank(pagamentoBean.getDataScadenza())
                || isBlank(pagamentoBean.getCvv())) {
            throw new PaymentException("Inserisci tutti i dati della carta.");
        }

        if (!pagamentoBean.getIntestatario().trim().matches("^[\\p{L}][\\p{L}\\s'\\-]*$")) {
            throw new PaymentException("Intestatario carta non valido.");
        }

        String numeroCarta = pagamentoBean.getNumeroCarta().replaceAll("\\s+", "");
        if (!numeroCarta.matches("\\d{13,19}")) {
            throw new PaymentException("Numero carta non valido.");
        }

        validaScadenzaCarta(pagamentoBean.getDataScadenza());

        if (!pagamentoBean.getCvv().matches("\\d{3,4}")) {
            throw new PaymentException("CVV non valido.");
        }
    }

    // La UI invia la scadenza come MM/YY o MM/YYYY.
    private void validaScadenzaCarta(String dataScadenza) throws PaymentException {
        if (!dataScadenza.matches("(0[1-9]|1[0-2])/(\\d{2}|\\d{4})")) {
            throw new PaymentException("Data di scadenza non valida. Usa MM/YY o MM/YYYY.");
        }

        String[] parti = dataScadenza.split("/");
        int mese = Integer.parseInt(parti[0]);
        int anno = Integer.parseInt(parti[1]);
        if (parti[1].length() == 2) {
            anno += 2000;
        }

        YearMonth scadenza = YearMonth.of(anno, mese);
        if (scadenza.isBefore(YearMonth.now())) {
            throw new PaymentException("La carta risulta scaduta.");
        }
    }

    private void validaDatiPaypal(PagamentoBean pagamentoBean) throws PaymentException {
        String email = pagamentoBean.getEmailPaypal();
        String confermaEmail = pagamentoBean.getConfermaEmailPaypal();

        if (isBlank(email) || isBlank(confermaEmail)) {
            throw new PaymentException("Inserisci e conferma l'email PayPal.");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new PaymentException("Email PayPal non valida.");
        }

        if (!email.equalsIgnoreCase(confermaEmail)) {
            throw new PaymentException("Le email PayPal non coincidono.");
        }
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String testoCampo(TextField campo) {
        return campo == null || campo.getText() == null ? "" : campo.getText().trim();
    }

    private void mostraPopupAcquistoCompletato(OrdineBean ordineBean, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificaOrdinePronto.fxml"));
            Parent root = loader.load();

            final boolean[] homeAperta = {false};
            NotificaOrdineProntoGraphicController popupController = loader.getController();
            popupController.configura(
                    ordineBean,
                    "Acquista Articolo",
                    "Ordine completato correttamente.",
                    "Torna alla Home",
                    "/Image/pagamento_approvato.png",
                    () -> {
                        homeAperta[0] = true;
                        ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, owner);
                    }
            );

            Stage popupStage = new Stage();
            popupStage.initOwner(owner);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Acquista Articolo");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            if (!homeAperta[0]) {
                ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, owner);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento popup acquisto completato.", e);
            mostraMessaggio("Acquista Articolo",
                    "Ordine completato correttamente.",
                    Alert.AlertType.INFORMATION);
            ViewSwitcher.switchTo("/view/MainView.fxml", sessionId, owner);
        }
    }
}
