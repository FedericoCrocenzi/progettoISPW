package it.ispw.project.application_controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcquistaArticoloControllerApplicativoTest {

    @Test
    void recuperaNotificheMerceProntaConSessioneAssenteRestituisceListaVuota() {
        AcquistaArticoloControllerApplicativo controller = new AcquistaArticoloControllerApplicativo();

        assertDoesNotThrow(() ->
                assertTrue(controller.recuperaNotificheMercePronta("sessione-non-esistente").isEmpty())
        );
    }
}
