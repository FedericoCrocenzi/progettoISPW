package it.ispw.project.model;

import it.ispw.project.model.observer.Subject;

public class GestoreNotifiche extends Subject {

    // Singleton
    private GestoreNotifiche() { super(); }

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
    public void inviaNotificaNuovoOrdine(Ordine ordine) {
        // Notifica gli observer passando l'oggetto Ordine
        super.notifyObservers(ordine);
    }

    /**
     * Usato per messaggi generici (es. "Cliente in negozio", "Merce Pronta")
     */
    public void inviaMessaggio(String messaggio) {
        // Notifica gli observer passando una Stringa
        super.notifyObservers(messaggio);
    }
}