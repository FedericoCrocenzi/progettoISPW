package it.ispw.project.graphicControllerCLI;

import it.ispw.project.sessionManager.SessionManager;

public class CLIViewNavigator {

    private static String currentSessionId;

    private CLIViewNavigator() {
        // utility class
    }

    // =========================
    // SESSION MANAGEMENT
    // =========================

    public static void setSessionId(String sessionId) {
        currentSessionId = sessionId;
    }

    public static String getSessionId() {
        return currentSessionId;
    }

    // =========================
    // NAVIGATION
    // =========================

    public static void goToLogin() {
        new LoginCLIController().show();
    }

    // -------- CLIENTE --------

    public static void goToCatalogo() {
        if (!checkSession()) return;
        CatalogoCLIController c = new CatalogoCLIController();
        c.initData(currentSessionId);
        c.show();
    }

    public static void goToCarrello() {
        if (!checkSession()) return;
        CarrelloCLIController c = new CarrelloCLIController();
        c.initData(currentSessionId);
        c.show();
    }

    public static void goToPagamento() {
        if (!checkSession()) return;
        PagamentoCLIController c = new PagamentoCLIController();
        c.initData(currentSessionId);
        c.show();
    }

    public static void goToProfilo() {
        if (!checkSession()) return;
        ProfiloCLIController c = new ProfiloCLIController();
        c.initData(currentSessionId);
        c.show();
    }

    // -------- COMMESSO --------

    public static void goToAreaCommesso() {
        if (!checkSession()) return;
        CommessoCLIController c = new CommessoCLIController();
        c.initData(currentSessionId);
        c.show();
    }

    // =========================
    // LOGOUT
    // =========================

    public static void logout() {
        if (currentSessionId != null) {
            SessionManager.getInstance().removeSession(currentSessionId);
            currentSessionId = null;
        }

        System.out.println("\nLogout effettuato correttamente.\n");
        goToLogin();
    }

    // =========================
    // SAFETY
    // =========================

    private static boolean checkSession() {
        if (currentSessionId == null) {
            System.out.println("Sessione non valida. Torno al login.");
            goToLogin();
            return false;
        }
        return true;
    }
}
