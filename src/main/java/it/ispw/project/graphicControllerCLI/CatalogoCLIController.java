package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.DAOException;

import java.util.List;

public class CatalogoCLIController extends CLIControllerBase {

    @Override
    public void show() {
        System.out.println("=== CATALOGO PRODOTTI ===");

        if (sessionId == null) {
            System.out.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        AcquistaArticoloControllerApplicativo controller =
                new AcquistaArticoloControllerApplicativo();

        List<ArticoloBean> catalogo;
        try {
            catalogo = controller.visualizzaCatalogo();
        } catch (DAOException e) {
            System.out.println("Errore nel caricamento del catalogo.");
            CLIViewNavigator.goToLogin();
            return;
        }

        for (int i = 0; i < catalogo.size(); i++) {
            ArticoloBean a = catalogo.get(i);
            System.out.println((i + 1) + ") "
                    + a.getDescrizione()
                    + " - €" + a.getPrezzo());
        }

        System.out.println();
        System.out.println("0) Vai al carrello");
        System.out.println("9) Logout");
        System.out.print("Scelta: ");

        int scelta;
        try {
            scelta = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            show();
            return;
        }

        if (scelta == 0) {
            CLIViewNavigator.goToCarrello();

        } else if (scelta == 9) {
            CLIViewNavigator.logout();

        } else if (scelta > 0 && scelta <= catalogo.size()) {
            ArticoloBean selezionato = catalogo.get(scelta - 1);
            ArticoloCLIController ctrl = new ArticoloCLIController(selezionato);
            ctrl.initData(sessionId);
            ctrl.show();

        } else {
            show();
        }
    }
}
