package it.ispw.project.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarrelloTest {

    private Carrello carrello;
    private Articolo zappa;
    private Articolo rastrello;

    @BeforeEach
    void setUp() {
        carrello = new Carrello();
        zappa = new Utensile(1, "Zappa", 12.50, 10, "Acciaio");
        rastrello = new Utensile(2, "Rastrello", 8.00, 5, "Ferro");
    }

    @Test
    void aggiungiArticoloInserisceArticoloEQuantita() {
        carrello.aggiungiArticolo(zappa, 2);

        assertEquals(2, carrello.getListaArticoli().get(zappa));
        assertEquals(1, carrello.getListaArticoli().size());
    }

    @Test
    void rimuoviArticoloEliminaArticoloDalCarrello() {
        carrello.aggiungiArticolo(zappa, 2);

        carrello.rimuoviArticolo(zappa);

        assertTrue(carrello.getListaArticoli().isEmpty());
    }

    @Test
    void calcolaTotaleSommaPrezzoPerQuantita() {
        carrello.aggiungiArticolo(zappa, 2);
        carrello.aggiungiArticolo(rastrello, 3);

        assertEquals(49.00, carrello.calcolaTotale(), 0.001);
    }

    @Test
    void svuotaRimuoveTuttiGliArticoli() {
        carrello.aggiungiArticolo(zappa, 2);
        carrello.aggiungiArticolo(rastrello, 1);

        carrello.svuota();

        assertTrue(carrello.getListaArticoli().isEmpty());
        assertEquals(0.0, carrello.calcolaTotale(), 0.001);
    }
}
