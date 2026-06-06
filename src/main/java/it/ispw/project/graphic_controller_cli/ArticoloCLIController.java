package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.QuantitaInsufficienteException;

public class ArticoloCLIController extends CLIControllerBase {

    private final ArticoloBean articolo;

    public ArticoloCLIController(ArticoloBean articolo) {
        this.articolo = articolo;
    }

    @Override
    public void show() {
        CLIPrinter.println("=== DETTAGLIO ARTICOLO ===");
        CLIPrinter.println("Descrizione: " + articolo.getDescrizione());
        CLIPrinter.println("Prezzo: €" + articolo.getPrezzo());

        // 🔐 sessionId DEVE arrivare dal navigator (come JavaFX)
        if (sessionId == null) {
            CLIPrinter.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        CLIPrinter.print("Quantità da aggiungere: ");
        int qta;
        try {
            qta = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            CLIPrinter.println("Inserisci un numero valido.");
            waitForEnter();
            show();
            return;
        }

        try {
            AcquistaArticoloControllerApplicativo controller =
                    new AcquistaArticoloControllerApplicativo();


            controller.aggiungiArticoloAlCarrello(sessionId, articolo, qta);

            CLIPrinter.println("Articolo aggiunto al carrello.");
            CLIViewNavigator.goToCatalogo();

        } catch (QuantitaInsufficienteException e) {
            CLIPrinter.println("Quantità non disponibile.");
            waitForEnter();
            show();
        } catch (DAOException e) {
            CLIPrinter.println("Errore di sistema durante l'aggiornamento del carrello.");
            waitForEnter();
            CLIViewNavigator.goToCatalogo();
        } catch (IllegalArgumentException e) {
            CLIPrinter.println("Errore durante l'aggiunta al carrello: " + e.getMessage());
            waitForEnter();
            CLIViewNavigator.goToCatalogo();
        }
    }
}
