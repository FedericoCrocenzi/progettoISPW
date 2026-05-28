package it.ispw.project.bean;

public class NotificaOrdineBean {

    private int idOrdine;
    private int idCliente;
    private String stato;
    private OrdineBean ordine;

    public int getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(int idOrdine) {
        this.idOrdine = idOrdine;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public OrdineBean getOrdine() {
        return ordine;
    }

    public void setOrdine(OrdineBean ordine) {
        this.ordine = ordine;
    }
}
