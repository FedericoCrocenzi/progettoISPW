package it.ispw.project.bean;

import it.ispw.project.exception.PaymentException;
import it.ispw.project.validation.PagamentoValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagamentoValidatorTest {

    @Test
    void pagamentoContantiValidoNonLanciaEccezioni() {
        PagamentoBean pagamento = new PagamentoBean();
        pagamento.setMetodoPagamento("CONTANTI_CONSEGNA");

        assertDoesNotThrow(() -> PagamentoValidator.valida(pagamento));
    }

    @Test
    void metodoPagamentoMancanteLanciaPaymentException() {
        PagamentoBean pagamento = new PagamentoBean();

        assertThrows(PaymentException.class, () -> PagamentoValidator.valida(pagamento));
    }

    @Test
    void emailPaypalNonValidaLanciaPaymentException() {
        PagamentoBean pagamento = new PagamentoBean();
        pagamento.setMetodoPagamento("PAYPAL");
        pagamento.setEmailPaypal("email-non-valida");
        pagamento.setPasswordPaypal("password");

        assertThrows(PaymentException.class, () -> PagamentoValidator.valida(pagamento));
    }
}
