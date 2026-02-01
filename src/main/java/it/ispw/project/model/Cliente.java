package it.ispw.project.model;

public class Cliente extends Utente {

    private String email;
    private String indirizzoSpedizione;

    public Cliente(String username, String password, String email, String indirizzo) {
        super(username, password); // Chiama il costruttore del padre
        this.email = email;
        this.indirizzoSpedizione = indirizzo;
    }

    public String getEmail() { return email; }
    public String getIndirizzoSpedizione() { return indirizzoSpedizione; }
}