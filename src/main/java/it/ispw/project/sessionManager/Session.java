package it.ispw.project.sessionManager;

import it.ispw.project.model.Carrello;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;

import java.util.UUID;

public class Session {

    private final String sessionId;

    // AGGIUNTO: Conserviamo l'intero oggetto Utente
    private Utente utenteCorrente;

    // Dati appiattiti (se ti servono per accesso rapido)
    private int userId;
    private String username;
    private String ruolo;
    private Ordine ultimoOrdineCreato;

    // Lo stato della sessione (Carrello)
    private Carrello carrelloCorrente;

    public Session(Utente utente) {
        this.sessionId = generateSessionId();

        // Assegnazione oggetto completo
        this.utenteCorrente = utente;

        // Assegnazione dati derivati
        this.userId = utente.ottieniId(); // Assicurati che Utente abbia questo metodo (o ottieniId)
        this.username = utente.leggiUsername();
        this.ruolo = utente.scopriRuolo();

        // Inizializza carrello vuoto
        this.carrelloCorrente = new Carrello();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    // --- NUOVO METODO NECESSARIO ---
    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }
    public void setUltimoOrdineCreato(Ordine ordine) {
        this.ultimoOrdineCreato = ordine;
    }

    public Ordine getUltimoOrdineCreato() {
        return this.ultimoOrdineCreato;
    }

    // --- Getters Esistenti ---
    public String getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRuolo() { return ruolo; }
    public Carrello getCarrelloCorrente() { return carrelloCorrente; }
}