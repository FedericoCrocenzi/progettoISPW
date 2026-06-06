package it.ispw.project.model;

public class NotificaOrdine {

    public enum Tipo {
        NUOVO_ORDINE,
        MERCE_PRONTA
    }

    private final Tipo tipo;
    private final int idOrdine;
    private final int idCliente;
    private final String stato;
    private final Ordine ordine;

    private NotificaOrdine(Tipo tipo, Ordine ordine) {
        this.tipo = tipo;
        this.ordine = ordine;
        this.idOrdine = ordine != null ? ordine.leggiId() : 0;
        this.idCliente = ordine != null && ordine.getCliente() != null ? ordine.getCliente().ottieniId() : 0;
        this.stato = ordine != null ? ordine.getStato() : null;
    }

    public static NotificaOrdine nuovoOrdine(Ordine ordine) {
        return new NotificaOrdine(Tipo.NUOVO_ORDINE, ordine);
    }

    public static NotificaOrdine mercePronta(Ordine ordine) {
        return new NotificaOrdine(Tipo.MERCE_PRONTA, ordine);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getIdOrdine() {
        return idOrdine;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getStato() {
        return stato;
    }

    public Ordine getOrdine() {
        return ordine;
    }
}
