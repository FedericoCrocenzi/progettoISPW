package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.DAOException;

import java.util.List;

public class CatalogoCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== CATALOGO PRODOTTI ===");

        if (sessioneNonValida("Errore: sessione non inizializzata.")) {
            return;
        }

        AcquistaArticoloControllerApplicativo controller = creaControllerAcquisto();

        List<ArticoloBean> catalogo;
        try {
            catalogo = controller.visualizzaCatalogo();
        } catch (DAOException e) {
            CLIPrinter.println("Errore di sistema durante il caricamento del catalogo.");
            waitForEnter();
            return;
        }

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

        Integer scelta = leggiSceltaIntera();
        if (scelta == null) {
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
