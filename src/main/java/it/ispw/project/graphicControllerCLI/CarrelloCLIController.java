package it.ispw.project.graphicControllerCLI;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.QuantitaInsufficienteException;

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

        for (int i = 0; i < carrello.getListaArticoli().size(); i++) {
            ArticoloBean articolo = carrello.getListaArticoli().get(i);
            System.out.println((i + 1) + ") " + articolo.getDescrizione() + " x" + articolo.getQuantita());
        }

        System.out.println("Totale: EUR " + carrello.getTotale());
        System.out.println();
        System.out.println("1) Procedi al pagamento");
        System.out.println("2) Aumenta quantita articolo");
        System.out.println("3) Diminuisci quantita articolo");
        System.out.println("4) Torna al catalogo");
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
                modificaQuantita(controller, carrello, true);
                break;
            case 3:
                modificaQuantita(controller, carrello, false);
                break;
            case 4:
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

    private void modificaQuantita(AcquistaArticoloControllerApplicativo controller,
                                  CarrelloBean carrello,
                                  boolean aumenta) {
        System.out.print("Numero articolo: ");
        try {
            int indice = Integer.parseInt(scanner.nextLine()) - 1;
            if (indice < 0 || indice >= carrello.getListaArticoli().size()) {
                System.out.println("Articolo non valido.");
                waitForEnter();
                show();
                return;
            }

            ArticoloBean articolo = carrello.getListaArticoli().get(indice);
            if (aumenta) {
                controller.aggiungiArticoloAlCarrello(sessionId, articolo, 1);
            } else {
                controller.diminuisciQuantitaArticoloDalCarrello(sessionId, articolo, 1);
            }
            show();

        } catch (NumberFormatException e) {
            System.out.println("Inserisci un numero valido.");
            waitForEnter();
            show();
        } catch (QuantitaInsufficienteException e) {
            System.out.println("Quantita non disponibile.");
            waitForEnter();
            show();
        } catch (DAOException | IllegalArgumentException e) {
            System.out.println("Impossibile aggiornare il carrello.");
            waitForEnter();
            show();
        }
    }
}
