package it.ispw.project.model;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class NotificaOrdineTest {

    @Test
    void creaNotificaNuovoOrdineConDatiOrdine() {
        Ordine ordine = creaOrdine("IN_ELABORAZIONE");

        NotificaOrdine notifica = NotificaOrdine.nuovoOrdine(ordine);

        assertEquals(NotificaOrdine.Tipo.NUOVO_ORDINE, notifica.getTipo());
        assertEquals(10, notifica.getIdOrdine());
        assertEquals(2, notifica.getIdCliente());
        assertEquals("IN_ELABORAZIONE", notifica.getStato());
        assertSame(ordine, notifica.getOrdine());
    }

    @Test
    void creaNotificaMerceProntaConDatiOrdine() {
        Ordine ordine = creaOrdine("PRONTO");

        NotificaOrdine notifica = NotificaOrdine.mercePronta(ordine);

        assertEquals(NotificaOrdine.Tipo.MERCE_PRONTA, notifica.getTipo());
        assertEquals(10, notifica.getIdOrdine());
        assertEquals(2, notifica.getIdCliente());
        assertEquals("PRONTO", notifica.getStato());
        assertSame(ordine, notifica.getOrdine());
    }

    private Ordine creaOrdine(String stato) {
        Utente cliente = new Utente(2, "cliente", "1234", "CLIENTE", "cliente@test.it", "Via Test 1");
        Ordine ordine = new Ordine(10, new Date(), cliente, new HashMap<>(), 0.0);
        ordine.setStato(stato);
        return ordine;
    }
}
