package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.PaymentException;

public class PagamentoCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== PAGAMENTO ===");

        if (sessionId == null) {
            CLIPrinter.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        AcquistaArticoloControllerApplicativo controller =
                new AcquistaArticoloControllerApplicativo();

        try {
            CarrelloBean carrello = controller.visualizzaCarrello(sessionId);
            if (carrello.getListaArticoli().isEmpty()) {
                CLIPrinter.println("Il carrello e' vuoto. Aggiungi almeno un articolo prima di pagare.");
                waitForEnter();
                CLIViewNavigator.goToCatalogo();
                return;
            }

            PagamentoBean pagamento = creaPagamentoDaInput();
            if (pagamento == null) {
                return;
            }
            pagamento.setImportoDaPagare(carrello.getTotale());

            OrdineBean ordine = controller.completaAcquisto(sessionId, pagamento);

            CLIPrinter.println("Pagamento completato con successo.");
            CLIPrinter.println("Ordine #" + ordine.getId()
                    + " - Totale: EUR " + ordine.getTotale());
            CLIPrinter.println("Notifica nuovo ordine inviata al commesso.");

            waitForEnter();
            CLIViewNavigator.goToCatalogo();

        } catch (PaymentException e) {
            CLIPrinter.println("Errore nel pagamento: " + e.getMessage());
            waitForEnter();
            CLIViewNavigator.goToCarrello();

        } catch (DAOException e) {
            CLIPrinter.println("Errore di sistema durante il pagamento.");
            waitForEnter();
            CLIViewNavigator.goToCarrello();
        }
    }

    private PagamentoBean creaPagamentoDaInput() throws PaymentException {
        CLIPrinter.println("1) Carta di credito");
        CLIPrinter.println("2) PayPal");
        CLIPrinter.println("3) Paga in cassa");
        CLIPrinter.println("0) Torna al carrello");
        CLIPrinter.print("Metodo: ");

        String scelta = scanner.nextLine();
        PagamentoBean pagamento = new PagamentoBean();

        switch (scelta) {
            case "1":
                pagamento.setMetodoPagamento("CARTA_CREDITO");
                CLIPrinter.print("Intestatario carta: ");
                pagamento.setIntestatario(scanner.nextLine().trim());
                CLIPrinter.print("Numero carta: ");
                pagamento.setNumeroCarta(scanner.nextLine().trim());
                CLIPrinter.print("Scadenza (MM/YY o MM/YYYY): ");
                pagamento.setDataScadenza(scanner.nextLine().trim());
                CLIPrinter.print("CVV: ");
                pagamento.setCvv(scanner.nextLine().trim());
                return pagamento;
            case "2":
                pagamento.setMetodoPagamento("PAYPAL");
                CLIPrinter.print("Email PayPal: ");
                pagamento.setEmailPaypal(scanner.nextLine().trim());
                CLIPrinter.print("Password PayPal: ");
                pagamento.setPasswordPaypal(scanner.nextLine().trim());
                return pagamento;
            case "3":
                pagamento.setMetodoPagamento("CONTANTI_CONSEGNA");
                return pagamento;
            case "0":
                CLIViewNavigator.goToCarrello();
                return null;
            default:
                throw new PaymentException("Metodo di pagamento non valido.");
        }
    }
}
