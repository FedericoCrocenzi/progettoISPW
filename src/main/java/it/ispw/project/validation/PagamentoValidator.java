package it.ispw.project.validation;

import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.exception.PaymentException;

import java.time.ZoneId;
import java.time.YearMonth;

public final class PagamentoValidator {

    private static final String METODO_CARTA_CREDITO = "CARTA_CREDITO";
    private static final String METODO_PAYPAL = "PAYPAL";
    private static final String METODO_CONTANTI_CONSEGNA = "CONTANTI_CONSEGNA";

    private PagamentoValidator() {
        // Utility class.
    }

    public static void valida(PagamentoBean datiPagamento) throws PaymentException {
        String metodo = datiPagamento.getMetodoPagamento();
        if (metodo == null || metodo.isBlank()) {
            throw new PaymentException("Seleziona un metodo di pagamento.");
        }

        if (METODO_CONTANTI_CONSEGNA.equals(metodo)) {
            return;
        }

        if (METODO_PAYPAL.equals(metodo)) {
            validaDatiPaypal(datiPagamento);
            return;
        }

        if (METODO_CARTA_CREDITO.equals(metodo)) {
            validaDatiCarta(datiPagamento);
            return;
        }

        throw new PaymentException("Metodo di pagamento non valido.");
    }

    private static void validaDatiCarta(PagamentoBean datiPagamento) throws PaymentException {
        if (isBlank(datiPagamento.getIntestatario())
                || isBlank(datiPagamento.getNumeroCarta())
                || isBlank(datiPagamento.getDataScadenza())
                || isBlank(datiPagamento.getCvv())) {
            throw new PaymentException("Inserisci tutti i dati della carta.");
        }

        if (!datiPagamento.getIntestatario().trim().matches("^[\\p{L}][\\p{L}\\s'\\-]*$")) {
            throw new PaymentException("Intestatario carta non valido.");
        }

        String numeroCarta = datiPagamento.getNumeroCarta().replaceAll("\\s+", "");
        if (!numeroCarta.matches("\\d{13,19}")) {
            throw new PaymentException("Numero carta non valido.");
        }

        validaScadenzaCarta(datiPagamento.getDataScadenza());

        if (!datiPagamento.getCvv().matches("\\d{3,4}")) {
            throw new PaymentException("CVV non valido.");
        }
    }

    // Supporta i formati MM/YY e MM/YYYY.
    private static void validaScadenzaCarta(String dataScadenza) throws PaymentException {
        if (!dataScadenza.matches("(0[1-9]|1[0-2])/(\\d{2}|\\d{4})")) {
            throw new PaymentException("Data di scadenza non valida. Usa MM/YY o MM/YYYY.");
        }

        String[] parti = dataScadenza.split("/");
        int mese = Integer.parseInt(parti[0]);
        int anno = Integer.parseInt(parti[1]);
        if (parti[1].length() == 2) {
            anno += 2000;
        }

        YearMonth scadenza = YearMonth.of(anno, mese);
        if (scadenza.isBefore(YearMonth.now(ZoneId.systemDefault()))) {
            throw new PaymentException("La carta risulta scaduta.");
        }
    }

    private static void validaDatiPaypal(PagamentoBean datiPagamento) throws PaymentException {
        String email = datiPagamento.getEmailPaypal();
        String password = datiPagamento.getPasswordPaypal();

        if (isBlank(email) || isBlank(password)) {
            throw new PaymentException("Inserisci email e password PayPal.");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new PaymentException("Email PayPal non valida.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
