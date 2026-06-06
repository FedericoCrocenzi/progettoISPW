package it.ispw.project.pattern;

import it.ispw.project.model.Articolo;
import it.ispw.project.model.Carrello;
import it.ispw.project.model.Utensile;
import it.ispw.project.model.observer.Observer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CarrelloObserverTest {

    @Test
    void observerRegistratoRiceveNotificaQuandoCarrelloCambia() {
        Carrello carrello = new Carrello();
        TestObserver observer = new TestObserver();
        Articolo zappa = new Utensile(1, "Zappa", 12.50, 10, "Acciaio");

        carrello.attach(observer);
        carrello.aggiungiArticolo(zappa, 1);

        assertEquals(1, observer.numeroNotifiche);
        assertSame(carrello, observer.ultimoPayload);
    }

    private static class TestObserver implements Observer {
        private int numeroNotifiche;
        private Object ultimoPayload;

        @Override
        public void update(Object subject) {
            numeroNotifiche++;
            ultimoPayload = subject;
        }
    }
}
