package it.ispw.project.dao.dbConnection;

public class Queries {

    // COSTRUTTORE PRIVATO
    // Impedisce di istanziare la classe (es. new Queries()) perché serve solo come contenitore di costanti.
    // Questo copia lo stile di "WanderWise".
    private Queries() {}

    // =================================================================================
    // QUERY TABELLA UTENTE
    // =================================================================================

    public static final String SELECT_UTENTE_BY_CREDS =
            "SELECT * FROM utente WHERE (username = ? OR email = ?) AND password = ?";

    public static final String SELECT_UTENTE_BY_ID =
            "SELECT * FROM utente WHERE id = ?";

    // =================================================================================
    // QUERY TABELLA ARTICOLO
    // =================================================================================

    public static final String SELECT_ARTICOLO_BY_ID =
            "SELECT * FROM articolo WHERE id = ?";

    public static final String SELECT_ALL_ARTICOLI =
            "SELECT * FROM articolo";

    public static final String UPDATE_ARTICOLO_SCORTA =
            "UPDATE articolo SET scorta = ? WHERE id = ?";

    // Usata per la ricerca dinamica (appendendo AND ...)
    public static final String SELECT_ARTICOLO_BASE =
            "SELECT * FROM articolo WHERE 1=1";

    // =================================================================================
    // QUERY TABELLA ORDINE
    // =================================================================================

    public static final String INSERT_ORDINE =
            "INSERT INTO ordine (data_creazione, totale, stato, id_cliente) VALUES (?, ?, ?, ?)";

    public static final String SELECT_ORDINE_BY_ID =
            "SELECT * FROM ordine WHERE id = ?";

    public static final String SELECT_ORDINI_BY_CLIENTE =
            "SELECT * FROM ordine WHERE id_cliente = ? ORDER BY data_creazione DESC";

    public static final String UPDATE_ORDINE_STATO =
            "UPDATE ordine SET stato = ? WHERE id = ?";

    // =================================================================================
    // QUERY TABELLA RIGA_ORDINE
    // =================================================================================

    public static final String INSERT_RIGA_ORDINE =
            "INSERT INTO riga_ordine (id_ordine, id_articolo, quantita, prezzo_unitario) VALUES (?, ?, ?, ?)";

    public static final String SELECT_RIGHE_BY_ORDINE =
            "SELECT * FROM riga_ordine WHERE id_ordine = ?";
}