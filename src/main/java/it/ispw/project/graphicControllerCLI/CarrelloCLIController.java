package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.model.Carrello;

public class CarrelloCLIController extends CLIControllerBase {

    @Override
    public void show() {
        System.out.println("=== CARRELLO ===");

        if (sessionId == null) {
            System.out.println("Sessione non valida.");
            CLIViewNavigator.goToLogin();
            return;
        }

        AcquistaArticoloControllerApplicativo controller =
                new AcquistaArticoloControllerApplicativo();

        CarrelloBean carrello;
        try {
            carrello = controller.visualizzaCarrello(sessionId);
        } catch (Exception e) {
            System.out.println("Errore nel recupero del carrello.");
            CLIViewNavigator.goToCatalogo();
            return;
        }

        if (carrello.getListaArticoli().isEmpty()) {
            System.out.println("Carrello vuoto.");
            CLIViewNavigator.goToCatalogo();
            return;
        }

        carrello.getListaArticoli().forEach(a ->
                System.out.println("- " + a.getDescrizione() + " x" + a.getQuantita())
        );

        System.out.println("Totale: €" + carrello.getTotale());
        System.out.println();
        System.out.println("1) Procedi al pagamento");
        System.out.println("2) Torna al catalogo");
        System.out.println("9) Logout");
        System.out.print("Scelta: ");

        int scelta;
        try {
            scelta = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            show();
            return;
        }

        switch (scelta) {
            case 1:
                CLIViewNavigator.goToPagamento();
                break;

            case 2:
                CLIViewNavigator.goToCatalogo();
                break;

            case 9:
                CLIViewNavigator.logout();
                break;

            default:
                show();
                break;
        }
    }
}
