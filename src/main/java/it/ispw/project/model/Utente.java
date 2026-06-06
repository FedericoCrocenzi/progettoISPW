package it.ispw.project.model;

/**
 * Model unico. Niente SETTER pubblici per rispettare l'Information Hiding.
 * Lo stato si definisce alla costruzione.
 */
public class Utente {

    private int id;
    private String username;
    private String password;
    private String ruolo;
    private String email;
    private String indirizzo;

    // Costruttore "Pieno" per quando leggiamo dal Database
    // Il DAO userà questo per istanziare l'oggetto in un colpo solo
    public Utente(int id, String username, String password, String ruolo, String email, String indirizzo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.email = email;
        this.indirizzo = indirizzo;
    }

    // --- GETTERS (Solo lettura, niente Setter) ---

    public int ottieniId() { return id; }
    public String leggiUsername() { return username; }
    public String ottieniPassword() { return password; }
    public String scopriRuolo() { return ruolo; }
    public String leggiEmail() { return email; }
    public String leggiIndirizzo() { return indirizzo; }

}
