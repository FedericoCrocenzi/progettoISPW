package it.ispw.project.dao.dbConnection;

public class Queries {

    // =================================================================================
    // QUERY TABELLA UTENTE
    // =================================================================================

    /**
     * Login: Cerca un utente che corrisponda allo username OPPURE alla email,
     * verificando contemporaneamente la password.
     * Parametri: 1=username/email, 2=username/email, 3=password
     */
    public static final String SELECT_UTENTE_BY_CREDS =
            "SELECT * FROM utente WHERE (username = ? OR email = ?) AND password = ?";

    /**
     * Recupera un utente tramite ID.
     * Parametri: 1=id
     */
    public static final String SELECT_UTENTE_BY_ID =
            "SELECT * FROM utente WHERE id = ?";

    // =================================================================================
    // QUERY TABELLA ARTICOLO
    // =================================================================================

    /**
     * Seleziona un articolo specifico tramite ID.
     * Parametri: 1=id
     */
    public static final String SELECT_ARTICOLO_BY_ID =
            "SELECT * FROM articolo WHERE id = ?";

    /**
     * Seleziona tutto il catalogo.
     */
    public static final String SELECT_ALL_ARTICOLI =
            "SELECT * FROM articolo";

    /**
     * Aggiorna la scorta di un articolo (usato dopo un acquisto o rifornimento).
     * Parametri: 1=nuova_scorta, 2=id
     */
    public static final String UPDATE_ARTICOLO_SCORTA =
            "UPDATE articolo SET scorta = ? WHERE id = ?";

    /**
     * Base per la ricerca filtrata (il Controller/DAO aggiungerà le WHERE dinamicamente).
     */
    public static final String SELECT_ARTICOLO_BASE =
            "SELECT * FROM articolo WHERE 1=1";

    // =================================================================================
    // QUERY TABELLA ORDINE
    // =================================================================================

    /**
     * Inserisce un nuovo ordine.
     * Parametri: 1=data_creazione, 2=totale, 3=stato, 4=id_cliente
     * Nota: Restituisce l'ID generato se usato con RETURN_GENERATED_KEYS.
     */
    public static final String INSERT_ORDINE =
            "INSERT INTO ordine (data_creazione, totale, stato, id_cliente) VALUES (?, ?, ?, ?)";

    /**
     * Seleziona un ordine tramite ID.
     * Parametri: 1=id
     */
    public static final String SELECT_ORDINE_BY_ID =
            "SELECT * FROM ordine WHERE id = ?";

    /**
     * Seleziona tutti gli ordini di un cliente specifico.
     * Parametri: 1=id_cliente
     */
    public static final String SELECT_ORDINI_BY_CLIENTE =
            "SELECT * FROM ordine WHERE id_cliente = ? ORDER BY data_creazione DESC";

    /**
     * Aggiorna lo stato di un ordine (es. da IN_ELABORAZIONE a COMPLETATO).
     * Parametri: 1=nuovo_stato, 2=id
     */
    public static final String UPDATE_ORDINE_STATO =
            "UPDATE ordine SET stato = ? WHERE id = ?";

    // =================================================================================
    // QUERY TABELLA RIGA_ORDINE
    // =================================================================================

    /**
     * Inserisce una riga dettaglio per un ordine.
     * Parametri: 1=id_ordine, 2=id_articolo, 3=quantita, 4=prezzo_unitario
     */
    public static final String INSERT_RIGA_ORDINE =
            "INSERT INTO riga_ordine (id_ordine, id_articolo, quantita, prezzo_unitario) VALUES (?, ?, ?, ?)";

    /**
     * Seleziona tutte le righe associate a un ordine.
     * Parametri: 1=id_ordine
     */
    public static final String SELECT_RIGHE_BY_ORDINE =
            "SELECT * FROM riga_ordine WHERE id_ordine = ?";
}