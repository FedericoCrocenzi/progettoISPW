package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.PaymentException;
import it.ispw.project.exception.DAOException;

public class PagamentoCLIController extends CLIControllerBase {

    @Override
    public void show() {
        System.out.println("=== PAGAMENTO ===");

        if (sessionId == null) {
            System.out.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        System.out.println("1) Carta");
        System.out.println("2) Contanti");
        System.out.print("Metodo: ");
        int scelta = Integer.parseInt(scanner.nextLine());

        PagamentoBean pagamento = new PagamentoBean();
        pagamento.setMetodoPagamento(scelta == 1 ? "CARTA" : "CONTANTI");

        try {
            AcquistaArticoloControllerApplicativo controller =
                    new AcquistaArticoloControllerApplicativo();

            // ✅ FIRMA REALE DEL METODO
            OrdineBean ordine =
                    controller.completaAcquisto(sessionId, pagamento);

            System.out.println("Pagamento completato con successo.");
            System.out.println("Ordine #" + ordine.getId()
                    + " - Totale: €" + ordine.getTotale());

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
}
