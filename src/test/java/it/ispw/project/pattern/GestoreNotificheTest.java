package it.ispw.project.pattern;

import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.model.NotificaOrdine;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;
import it.ispw.project.model.observer.Observer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class GestoreNotificheTest {

    @Test
    void getInstanceRestituisceIstanzaNonNull() {
        assertNotNull(GestoreNotifiche.getInstance());
    }

    @Test
    void getInstanceRestituisceSempreLaStessaIstanza() {
        GestoreNotifiche primaIstanza = GestoreNotifiche.getInstance();
        GestoreNotifiche secondaIstanza = GestoreNotifiche.getInstance();

        assertSame(primaIstanza, secondaIstanza);
    }

    @Test
    void observerRegistratoRiceveNotificaOrdine() {
        GestoreNotifiche gestore = GestoreNotifiche.getInstance();
        TestObserver observer = new TestObserver();
        NotificaOrdine notifica = NotificaOrdine.nuovoOrdine(creaOrdine());

        gestore.attach(observer);
        try {
            gestore.inviaNotificaNuovoOrdine(notifica);

            assertSame(notifica, observer.ultimoPayload);
        } finally {
            gestore.detach(observer);
        }
    }

    private Ordine creaOrdine() {
        Utente cliente = new Utente(2, "cliente", "1234", "CLIENTE", "cliente@test.it", "Via Test 1");
        return new Ordine(10, new Date(), cliente, new HashMap<>(), 0.0);
    }

    private static class TestObserver implements Observer {
        private Object ultimoPayload;

        @Override
        public void update(Object subject) {
            ultimoPayload = subject;
        }
    }
}
