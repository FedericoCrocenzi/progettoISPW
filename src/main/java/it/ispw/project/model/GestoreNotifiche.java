package it.ispw.project.model;

import it.ispw.project.bean.NotificaOrdineBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.model.observer.Subject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GestoreNotifiche extends Subject {

    private final Map<Integer, NotificaOrdineBean> notificheMercePronta;

    // Singleton
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
    public void inviaNotificaNuovoOrdine(OrdineBean ordine) {
        super.notifyObservers(ordine);
    }

    public void inviaNotificaMercePronta(NotificaOrdineBean notifica) {
        if (notifica == null || notifica.getIdOrdine() <= 0 || notifica.getIdCliente() <= 0) {
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

    public synchronized List<NotificaOrdineBean> getNotificheMerceProntaPerCliente(int idCliente) {
        List<NotificaOrdineBean> notifiche = new ArrayList<>();
        for (NotificaOrdineBean notifica : notificheMercePronta.values()) {
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
