package it.ispw.project.session_manager;

import it.ispw.project.model.Carrello;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;

import java.util.UUID;

public class Session {

    private final String sessionId;

    private Utente utenteCorrente;

    private int userId;
    private String username;
    private String ruolo;
    private Ordine ultimoOrdineCreato;

    private Carrello carrelloCorrente;

    public Session(Utente utente) {
        this.sessionId = generateSessionId();

        this.utenteCorrente = utente;

        this.userId = utente.ottieniId();
        this.username = utente.leggiUsername();
        this.ruolo = utente.scopriRuolo();

        this.carrelloCorrente = new Carrello();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }
    public void setUltimoOrdineCreato(Ordine ordine) {
        this.ultimoOrdineCreato = ordine;
    }

    public Ordine getUltimoOrdineCreato() {
        return this.ultimoOrdineCreato;
    }

    public String getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRuolo() { return ruolo; }
    public Carrello getCarrelloCorrente() { return carrelloCorrente; }
}
