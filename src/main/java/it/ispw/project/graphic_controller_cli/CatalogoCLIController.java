package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;

import java.util.List;

public class CatalogoCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== CATALOGO PRODOTTI ===");

        if (sessionId == null) {
            CLIPrinter.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        AcquistaArticoloControllerApplicativo controller =
                new AcquistaArticoloControllerApplicativo();

        List<ArticoloBean> catalogo = controller.visualizzaCatalogo();

        for (int i = 0; i < catalogo.size(); i++) {
            ArticoloBean a = catalogo.get(i);
            CLIPrinter.println((i + 1) + ") "
                    + a.getDescrizione()
                    + " - €" + a.getPrezzo());
        }

        CLIPrinter.println();
        CLIPrinter.println("0) Vai al carrello");
        CLIPrinter.println("9) Logout");
        CLIPrinter.print("Scelta: ");

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
