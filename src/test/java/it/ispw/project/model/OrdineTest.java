package it.ispw.project.model;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdineTest {

    private static final long DATA_CREAZIONE_TEST = 1_700_000_000_000L;

    @Test
    void nuovoOrdineHaStatoInElaborazione() {
        Ordine ordine = creaOrdine();

        assertEquals("IN_ELABORAZIONE", ordine.getStato());
    }

    @Test
    void completaOrdineImpostaStatoCompletato() {
        Ordine ordine = creaOrdine();

        ordine.completaOrdine();

        assertEquals("COMPLETATO", ordine.getStato());
    }

    @Test
    void articoliAcquistatiContieneArticoloPresente() {
        Articolo zappa = new Utensile(1, "Zappa", 12.50, 10, "Acciaio");
        Map<Articolo, Integer> articoli = new HashMap<>();
        articoli.put(zappa, 2);

        Ordine ordine = new Ordine(1, new Date(DATA_CREAZIONE_TEST), creaCliente(), articoli, 25.00);

        assertTrue(ordine.getArticoli().containsKey(zappa));
        assertEquals(2, ordine.getArticoli().get(zappa));
    }

    private Ordine creaOrdine() {
        return new Ordine(1, new Date(DATA_CREAZIONE_TEST), creaCliente(), new HashMap<>(), 0.0);
    }

    private Utente creaCliente() {
        return new Utente(2, "cliente", "1234", "CLIENTE", "cliente@test.it", "Via Test 1");
    }
}
