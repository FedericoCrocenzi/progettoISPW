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

    // Costruttore ridotto per la registrazione (quando ancora non abbiamo ID, email, etc.)
    // Utile se devi creare un utente nuovo da salvare poi nel DB
    public Utente(String username, String password, String ruolo) {
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
    }

    // --- GETTERS (Solo lettura, niente Setter) ---

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRuolo() { return ruolo; }
    public String getEmail() { return email; }
    public String getIndirizzo() { return indirizzo; }

    // N.B. Se in futuro serve modificare l'email, creerai un metodo di business:
    // public void aggiornaProfilo(String email, String indirizzo) { ... }
}