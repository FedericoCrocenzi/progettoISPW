package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;

import java.util.List;

public class CommessoCLIController extends CLIControllerBase {

    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        super.initData(sessionId);
        this.appController = new AcquistaArticoloControllerApplicativo();
    }

    @Override
    public void show() {
        CLIPrinter.println("\n=== AREA COMMESSO ===");
        CLIPrinter.println("1 - Visualizza ordini pendenti");
        CLIPrinter.println("2 - Conferma merce pronta");
        CLIPrinter.println("3 - Logout");

        CLIPrinter.print("Scelta: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> mostraOrdiniPendenti();
            case "2" -> confermaMercePronta();
            case "3" -> CLIViewNavigator.logout();
            default -> {
                CLIPrinter.println("Scelta non valida.");
                waitForEnter();
                show();
            }
        }
    }

    // =========================
    // FUNZIONALITÀ COMMESSO
    // =========================

    private void mostraOrdiniPendenti() {
        try {
            List<OrdineBean> ordini = appController.recuperaOrdiniPendenti();

            if (ordini.isEmpty()) {
                CLIPrinter.println("Nessun ordine da evadere.");
            } else {
                CLIPrinter.println("\nOrdini pendenti:");
                for (OrdineBean o : ordini) {
                    CLIPrinter.printf(
                            "- ID: %d | Totale: € %.2f | Stato: %s%n",
                            o.getId(),
                            o.getTotale(),
                            o.getStato()
                    );
                }
            }

        } catch (DAOException e) {
            CLIPrinter.println("Errore nel recupero ordini: " + e.getMessage());
        }

        waitForEnter();
        show();
    }

    private void confermaMercePronta() {
        CLIPrinter.print("Inserisci ID ordine da confermare: ");
        String input = scanner.nextLine();

        try {
            int idOrdine = Integer.parseInt(input);
            appController.confermaMercePronta(idOrdine);
            CLIPrinter.println("Ordine confermato. Cliente notificato.");

        } catch (NumberFormatException e) {
            CLIPrinter.println("ID non valido.");
        } catch (DAOException e) {
            CLIPrinter.println("Errore: " + e.getMessage());
        }

        waitForEnter();
        show();
    }
}
