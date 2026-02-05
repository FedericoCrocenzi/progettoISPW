package it.ispw.project.bean;

public class UtenteBean {

    private String username;
    private String password; // Necessaria in fase di Login
    private String ruolo;    // "CLIENTE" o "COMMESSO"

    // Dati specifici del cliente (null se è un commesso)
    private String email;
    private String indirizzo;
    private String sessionId;

    public UtenteBean() {}

    // --- Getters & Setters ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

}