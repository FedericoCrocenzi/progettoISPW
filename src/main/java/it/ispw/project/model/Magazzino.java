package it.ispw.project.model;


import java.util.HashMap;
import java.util.Map;

/**
 * Classe Singleton che rappresenta l'inventario fisico del negozio.
 * Pattern: Singleton con Lazy Initialization (Bill Pugh / Inner Static Helper).
 * * Responsabilità:
 * 1. Mantenere lo stato corrente degli articoli in memoria.
 * 2. Fornire metodi per la ricerca e l'aggiornamento delle scorte.
 */
public class Magazzino {

    // Struttura dati in memoria: ID Articolo -> Oggetto Articolo
    // Usiamo una Map per avere complessità O(1) nella ricerca per ID.
    private Map<Integer, Articolo> stock;

    /**
     * Costruttore privato per impedire l'istanziazione esterna.
     * Inizializza la struttura dati.
     */
    private Magazzino() {
        this.stock = new HashMap<>();
        // In uno scenario reale, qui (o tramite un metodo init)
        // il magazzino si riempirebbe chiamando il DAO.
    }

    /**
     * INNER CLASS STATICA per il Singleton.
     * Questa classe viene caricata in memoria solo quando viene invocato getInstance().
     * Garantisce thread-safety automatica da parte della JVM.
     */
    private static class MagazzinoHelper {
        private static final Magazzino INSTANCE = new Magazzino();
    }

    /**
     * Punto di accesso globale all'istanza.
     */
    public static Magazzino getInstance() {
        return MagazzinoHelper.INSTANCE;
    }

    // --- METODI DI GESTIONE ARTICOLI ---

    /**
     * Aggiunge o aggiorna un articolo nel magazzino.
     * @param articolo L'articolo da inserire.
     */
    public void aggiungiArticolo(Articolo articolo) {
        if (articolo != null) {
            this.stock.put(articolo.leggiId(), articolo);
        }
    }

    /**
     * Cerca un articolo per ID.
     * @param id L'identificativo dell'articolo.
     * @return L'oggetto Articolo o null se non trovato.
     */
    public Articolo trovaArticolo(int id) {
        return this.stock.get(id);
    }

    /**
     * Verifica se c'è abbastanza scorta per un dato articolo.
     * Delega il controllo specifico all'oggetto Articolo stesso (Information Hiding).
     */
    public boolean verificaDisponibilita(int idArticolo, int quantitaRichiesta) {
        Articolo art = this.stock.get(idArticolo);
        if (art == null) {
            return false; // Articolo non esiste
        }
        return art.checkDisponibilita(quantitaRichiesta);
    }

    /**
     * Decrementa la scorta di un articolo dopo un acquisto.
     * Importante: Questo metodo aggiorna il modello in memoria.
     * Il Controller si occuperà poi di persistere questo cambiamento sul DB tramite DAO.
     */
    public void scaricaMerce(int idArticolo, int quantita) {
        Articolo art = this.stock.get(idArticolo);
        if (art != null) {
            // Qui stiamo usando un valore negativo per "aggiornaScorta" per sottrarre
            art.aggiornaScorta(-quantita);
        }
    }

    /**
     * Metodo di utilità per ottenere tutto il catalogo (es. per visualizzarlo).
     */
    public Map<Integer, Articolo> getCatalogo() {
        return this.stock;
    }
}