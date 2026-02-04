package it.ispw.project.bean;

public class PagamentoBean {

    // Metodo scelto: "CARTA_CREDITO", "PAYPAL", "CONTANTI_CONSEGNA"
    private String metodoPagamento;

    private double importoDaPagare; // Utile per verifica di coerenza

    // --- Dati per Carta di Credito (Opzionali/Nullable) ---
    private String numeroCarta;
    private String intestatario;
    private String dataScadenza; // Formato stringa "MM/YY" è spesso sufficiente per il bean
    private String cvv;

    // Costruttore vuoto
    public PagamentoBean() {}

    // --- Getters & Setters ---

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public double getImportoDaPagare() { return importoDaPagare; }
    public void setImportoDaPagare(double importoDaPagare) { this.importoDaPagare = importoDaPagare; }

    public String getNumeroCarta() { return numeroCarta; }
    public void setNumeroCarta(String numeroCarta) { this.numeroCarta = numeroCarta; }

    public String getIntestatario() { return intestatario; }
    public void setIntestatario(String intestatario) { this.intestatario = intestatario; }

    public String getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(String dataScadenza) { this.dataScadenza = dataScadenza; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}