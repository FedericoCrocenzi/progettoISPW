package it.ispw.project.sessionManager;

import it.ispw.project.model.Carrello;
import it.ispw.project.model.Utente;

import java.util.UUID;

public class Session {

    private final String sessionId;

    // Dati dell'utente appiattiti (stile esempio) o oggetto intero
    private int userId;
    private String username;
    private String ruolo; // Nel tuo esempio era roleEnum, qui usiamo String per coerenza con Utente

    // LO STATO DELLA SESSIONE: Il Carrello!
    // Sostituisce "actualGuidedTour" del tuo esempio
    private Carrello carrelloCorrente;

    public Session(Utente utente) {
        this.sessionId = generateSessionId();
        this.userId = utente.getId();
        this.username = utente.getUsername();
        this.ruolo = utente.getRuolo();

        // Ogni nuova sessione nasce con un carrello vuoto pronto all'uso
        this.carrelloCorrente = new Carrello();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    // --- Getters & Setters ---

    public String getSessionId() {
        return sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRuolo() {
        return ruolo;
    }

    public Carrello getCarrelloCorrente() {
        return carrelloCorrente;
    }

    // Utile se vogliamo resettare il carrello senza distruggere la sessione
    public void resetCarrello() {
        this.carrelloCorrente = new Carrello();
    }
}