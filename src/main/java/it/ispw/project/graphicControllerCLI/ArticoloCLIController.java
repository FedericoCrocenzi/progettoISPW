package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.exception.QuantitaInsufficienteException;
import it.ispw.project.exception.DAOException;

public class ArticoloCLIController extends CLIControllerBase {

    private final ArticoloBean articolo;

    public ArticoloCLIController(ArticoloBean articolo) {
        this.articolo = articolo;
    }

    @Override
    public void show() {
        System.out.println("=== DETTAGLIO ARTICOLO ===");
        System.out.println("Descrizione: " + articolo.getDescrizione());
        System.out.println("Prezzo: €" + articolo.getPrezzo());

        // 🔐 sessionId DEVE arrivare dal navigator (come JavaFX)
        if (sessionId == null) {
            System.out.println("Errore: sessione non inizializzata.");
            CLIViewNavigator.goToLogin();
            return;
        }

        System.out.print("Quantità da aggiungere: ");
        int qta;
        try {
            qta = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Inserisci un numero valido.");
            waitForEnter();
            show();
            return;
        }

        try {
            AcquistaArticoloControllerApplicativo controller =
                    new AcquistaArticoloControllerApplicativo();

            // ✅ CHIAMATA CORRETTA (firma reale)
            controller.aggiungiArticoloAlCarrello(sessionId, articolo, qta);

            System.out.println("Articolo aggiunto al carrello.");
            CLIViewNavigator.goToCatalogo();

        } catch (QuantitaInsufficienteException e) {
            System.out.println("Quantità non disponibile.");
            waitForEnter();
            show();
        } catch (DAOException | IllegalArgumentException e) {
            System.out.println("Errore durante l'aggiunta al carrello: " + e.getMessage());
            waitForEnter();
            CLIViewNavigator.goToCatalogo();
        }
    }
}
