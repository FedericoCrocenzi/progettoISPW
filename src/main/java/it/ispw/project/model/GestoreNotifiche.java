package it.ispw.project.model;

import it.ispw.project.model.observer.Subject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Singleton intenzionale: centralizza notifiche/eventi tra controller grafici nel progetto ISPW.
@SuppressWarnings("java:S6548")
public class GestoreNotifiche extends Subject {

    private final Map<Integer, NotificaOrdine> notificheMercePronta;

    private GestoreNotifiche() {
        super();
        this.notificheMercePronta = new LinkedHashMap<>();
    }

    private static class GestoreNotificheHelper {
        private static final GestoreNotifiche INSTANCE = new GestoreNotifiche();
    }

    public static GestoreNotifiche getInstance() {
        return GestoreNotificheHelper.INSTANCE;
    }

    // --- METODI DI NOTIFICA ---

    /**
     * Usato quando viene creato un nuovo ordine (Notifica al Magazzino/Commesso)
     */
    public void inviaNotificaNuovoOrdine(NotificaOrdine notifica) {
        if (notifica == null || notifica.getTipo() != NotificaOrdine.Tipo.NUOVO_ORDINE) {
            return;
        }
        super.notifyObservers(notifica);
    }

    public void inviaNotificaMercePronta(NotificaOrdine notifica) {
        if (notifica == null || notifica.getIdOrdine() <= 0 || notifica.getIdCliente() <= 0) {
            return;
        }
        if (notifica.getTipo() != NotificaOrdine.Tipo.MERCE_PRONTA) {
            return;
        }
        if (!"PRONTO".equals(notifica.getStato())) {
            return;
        }

        synchronized (this) {
            notificheMercePronta.put(notifica.getIdOrdine(), notifica);
        }
        super.notifyObservers(notifica);
    }

    public synchronized List<NotificaOrdine> getNotificheMerceProntaPerCliente(int idCliente) {
        List<NotificaOrdine> notifiche = new ArrayList<>();
        for (NotificaOrdine notifica : notificheMercePronta.values()) {
            if (notifica.getIdCliente() == idCliente) {
                notifiche.add(notifica);
            }
        }
        return notifiche;
    }

    public synchronized void rimuoviNotificaMercePronta(int idOrdine) {
        notificheMercePronta.remove(idOrdine);
    }

    /**
     * Usato per messaggi generici (es. "Cliente in negozio", "Merce Pronta")
     */
    public void inviaMessaggio(String messaggio) {
        // Notifica gli observer passando una Stringa
        super.notifyObservers(messaggio);
    }
}
