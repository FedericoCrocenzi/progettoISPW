package it.ispw.project.sessionManager;

import it.ispw.project.model.Utente;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionManager {

    private final Logger logger = Logger.getLogger(SessionManager.class.getName());

    // Mappa: SessionID (String) -> Oggetto Sessione
    private final Map<String, Session> activeSessions;

    private SessionManager() {
        activeSessions = new HashMap<>();
    }

    // Pattern Singleton con Helper Class (Stile del tuo esempio)
    private static class SingletonHelper {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    public static SessionManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Crea una nuova sessione per l'utente loggato.
     * Se l'utente ha già una sessione attiva, ritorna quella esistente.
     */
    public String addSession(Utente utente) {
        // Controllo duplicati (stesso utente loggato due volte)
        Session existingSession = checkDuplicateSessionUtente(utente);

        if (existingSession != null) {
            logger.log(Level.WARNING, "L'utente {0} ha già una sessione attiva.", utente.getUsername());
            return existingSession.getSessionId();
        }

        // Creazione nuova sessione
        Session session = new Session(utente);
        String sessionId = session.getSessionId();
        activeSessions.put(sessionId, session);

        logger.log(Level.INFO, "Nuova sessione creata: {0} per utente {1}", new Object[]{sessionId, utente.getUsername()});
        return sessionId;
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
        logger.log(Level.INFO, "Sessione rimossa: {0}", sessionId);
    }

    public Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Cerca se esiste già una sessione per questo username/email.
     * Usa gli stream come nel tuo esempio.
     */
    private Session checkDuplicateSessionUtente(Utente utente) {
        return activeSessions.values()
                .stream()
                // Controllo per username (univoco)
                .filter(session -> session.getUsername().equals(utente.getUsername()))
                .findFirst()
                .orElse(null);
    }
}