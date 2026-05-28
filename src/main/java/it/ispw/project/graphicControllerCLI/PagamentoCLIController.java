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
        System.out.println("=== PAGAMENTO ===");

        if (sessionId == null) {
            System.out.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        AcquistaArticoloControllerApplicativo controller =
                new AcquistaArticoloControllerApplicativo();

        try {
            CarrelloBean carrello = controller.visualizzaCarrello(sessionId);
            if (carrello.getListaArticoli().isEmpty()) {
                System.out.println("Il carrello e' vuoto. Aggiungi almeno un articolo prima di pagare.");
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

            System.out.println("Pagamento completato con successo.");
            System.out.println("Ordine #" + ordine.getId()
                    + " - Totale: EUR " + ordine.getTotale());
            System.out.println("Notifica nuovo ordine inviata al commesso.");

            waitForEnter();
            CLIViewNavigator.goToCatalogo();

        } catch (PaymentException e) {
            System.out.println("Errore nel pagamento: " + e.getMessage());
            waitForEnter();
            CLIViewNavigator.goToCarrello();

        } catch (DAOException e) {
            System.out.println("Errore di sistema durante il pagamento.");
            waitForEnter();
            CLIViewNavigator.goToCarrello();
        }
    }

    private PagamentoBean creaPagamentoDaInput() throws PaymentException {
        System.out.println("1) Carta di credito");
        System.out.println("2) PayPal");
        System.out.println("3) Paga in cassa");
        System.out.println("0) Torna al carrello");
        System.out.print("Metodo: ");

        String scelta = scanner.nextLine();
        PagamentoBean pagamento = new PagamentoBean();

        switch (scelta) {
            case "1":
                pagamento.setMetodoPagamento("CARTA_CREDITO");
                System.out.print("Intestatario carta: ");
                pagamento.setIntestatario(scanner.nextLine().trim());
                System.out.print("Numero carta: ");
                pagamento.setNumeroCarta(scanner.nextLine().trim());
                System.out.print("Scadenza (MM/YY o MM/YYYY): ");
                pagamento.setDataScadenza(scanner.nextLine().trim());
                System.out.print("CVV: ");
                pagamento.setCvv(scanner.nextLine().trim());
                return pagamento;
            case "2":
                pagamento.setMetodoPagamento("PAYPAL");
                System.out.print("Email PayPal: ");
                pagamento.setEmailPaypal(scanner.nextLine().trim());
                System.out.print("Conferma email PayPal: ");
                pagamento.setConfermaEmailPaypal(scanner.nextLine().trim());
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
