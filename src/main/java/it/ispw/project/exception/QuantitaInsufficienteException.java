package it.ispw.project.exception;

public class QuantitaInsufficienteException extends Exception {

    public QuantitaInsufficienteException(String message) {
        super(message);
    }

    // Costruttore opzionale per passare anche i dettagli
    public QuantitaInsufficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}