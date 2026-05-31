package it.ispw.project.graphic_controller_cli;

import it.ispw.project.application_controller.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.exception.QuantitaInsufficienteException;

public class CarrelloCLIController extends CLIControllerBase {

    @Override
    public void show() {
        CLIPrinter.println("=== CARRELLO ===");

        if (sessioneNonValida("Sessione non valida.")) {
            return;
        }

        AcquistaArticoloControllerApplicativo controller = creaControllerAcquisto();

        CarrelloBean carrello;
        try {
            carrello = controller.visualizzaCarrello(sessionId);
        } catch (Exception e) {
            CLIPrinter.println("Errore nel recupero del carrello.");
            CLIViewNavigator.goToCatalogo();
            return;
        }

        if (carrello.getListaArticoli().isEmpty()) {
            CLIPrinter.println("Carrello vuoto.");
            CLIViewNavigator.goToCatalogo();
            return;
        }

        for (int i = 0; i < carrello.getListaArticoli().size(); i++) {
            ArticoloBean articolo = carrello.getListaArticoli().get(i);
            CLIPrinter.println((i + 1) + ") " + articolo.getDescrizione() + " x" + articolo.getQuantita());
        }

        CLIPrinter.println("Totale: EUR " + carrello.getTotale());
        CLIPrinter.println();
        CLIPrinter.println("1) Procedi al pagamento");
        CLIPrinter.println("2) Aumenta quantita articolo");
        CLIPrinter.println("3) Diminuisci quantita articolo");
        CLIPrinter.println("4) Torna al catalogo");
        CLIPrinter.println("9) Logout");
        CLIPrinter.print("Scelta: ");

        Integer scelta = leggiSceltaIntera();
        if (scelta == null) {
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
        CLIPrinter.print("Numero articolo: ");
        try {
            int indice = Integer.parseInt(scanner.nextLine()) - 1;
            if (indice < 0 || indice >= carrello.getListaArticoli().size()) {
                mostraErroreAggiornamento("Articolo non valido.");
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
            mostraErroreAggiornamento("Inserisci un numero valido.");
        } catch (QuantitaInsufficienteException e) {
            mostraErroreAggiornamento("Quantita non disponibile.");
        } catch (IllegalArgumentException e) {
            mostraErroreAggiornamento("Impossibile aggiornare il carrello.");
        }
    }

    private void mostraErroreAggiornamento(String messaggio) {
        CLIPrinter.println(messaggio);
        waitForEnter();
        show();
    }
}
