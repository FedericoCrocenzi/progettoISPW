package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;

import java.util.List;

public class CommessoCLIController extends CLIControllerBase {

    private String sessionId;
    private AcquistaArticoloControllerApplicativo appController;

    @Override
    public void initData(String sessionId) {
        this.sessionId = sessionId;
        this.appController = new AcquistaArticoloControllerApplicativo();
    }

    @Override
    public void show() {
        System.out.println("\n=== AREA COMMESSO ===");
        System.out.println("1 - Visualizza ordini pendenti");
        System.out.println("2 - Conferma ritiro merce");
        System.out.println("3 - Logout");

        System.out.print("Scelta: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> mostraOrdiniPendenti();
            case "2" -> confermaMercePronta();
            case "3" -> CLIViewNavigator.logout();
            default -> {
                System.out.println("Scelta non valida.");
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
                System.out.println("Nessun ordine da evadere.");
            } else {
                System.out.println("\nOrdini pendenti:");
                for (OrdineBean o : ordini) {
                    System.out.printf(
                            "- ID: %d | Totale: € %.2f | Stato: %s%n",
                            o.getId(),
                            o.getTotale(),
                            o.getStato()
                    );
                }
            }

        } catch (DAOException e) {
            System.out.println("Errore nel recupero ordini: " + e.getMessage());
        }

        waitForEnter();
        show();
    }

    private void confermaMercePronta() {
        System.out.print("Inserisci ID ordine da confermare: ");
        String input = scanner.nextLine();

        try {
            int idOrdine = Integer.parseInt(input);
            appController.confermaRitiroMerce(idOrdine);
            System.out.println("Ordine confermato. Cliente notificato.");

        } catch (NumberFormatException e) {
            System.out.println("ID non valido.");
        } catch (DAOException e) {
            System.out.println("Errore: " + e.getMessage());
        }

        waitForEnter();
        show();
    }
}
