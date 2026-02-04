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

    // --- METODI DI BUSINESS ---

    /**
     * Invia una notifica PUSH al commesso con l'ordine appena creato.
     */
    public void inviaNotificaNuovoOrdine(Ordine ordine) {
        // Creiamo un semplice messaggio/oggetto wrapper o passiamo direttamente l'ordine
        // Per semplicità e chiarezza passiamo l'Ordine.
        // La View riceverà l'oggetto Ordine direttamente nel metodo update(Object data).
        super.notifyObservers(ordine);
    }

    // Non servono più i metodi getter (getOrdineOggettoDellaNotifica),
    // perché i dati arrivano direttamente!
}