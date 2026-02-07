package it.ispw.project.applicationController;

import it.ispw.project.bean.*;
import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.exception.PaymentException;
import it.ispw.project.exception.QuantitaInsufficienteException;
import it.ispw.project.model.*;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Controller Applicativo per il caso d'uso "Acquista Articolo".
 * Gestisce la logica di carrello, pagamento, creazione ordine e notifiche tra Cliente e Commesso.
 */
public class AcquistaArticoloControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;
    private Carrello carrello;
    private String sessionId;

    /**
     * Costruttore per utente loggato (con sessione).
     */
    public AcquistaArticoloControllerApplicativo(String sessionId) {
        this.sessionId = sessionId;
        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session != null) {
            this.carrello = session.getCarrelloCorrente();
        } else {
            this.carrello = new Carrello(); // Fallback
        }
    }

    /**
     * Costruttore base (es. per test o accesso anonimo al catalogo).
     */
    public AcquistaArticoloControllerApplicativo() {
        this.carrello = new Carrello();
    }

    // -----------------------------------------------------------------
    // GESTIONE CATALOGO E CARRELLO
    // -----------------------------------------------------------------

    public List<ArticoloBean> visualizzaCatalogo() throws DAOException {
        return ricercaArticoli(null);
    }

    public List<ArticoloBean> ricercaArticoli(RicercaArticoloBean criteri) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        List<Articolo> listaModel;

        if (criteri == null) {
            listaModel = articoloDAO.selectAllArticoli();
        } else {
            listaModel = articoloDAO.selectByFilter(
                    criteri.getTestoRicerca(),
                    criteri.getTipoArticolo(),
                    criteri.getPrezzoMin(),
                    criteri.getPrezzoMax()
            );
        }

        List<ArticoloBean> listaBean = new ArrayList<>();
        for (Articolo a : listaModel) {
            listaBean.add(convertiModelInBean(a));
        }
        return listaBean;
    }

    public void aggiungiArticoloAlCarrello(ArticoloBean articoloBean, int quantita)
            throws IllegalArgumentException, DAOException, QuantitaInsufficienteException {

        if (quantita <= 0) throw new IllegalArgumentException("La quantità deve essere positiva.");

        Magazzino magazzino = Magazzino.getInstance();
        Articolo articoloModel = magazzino.trovaArticolo(articoloBean.getId());

        // Se non è in cache nel Magazzino, lo recupero dal DB
        if (articoloModel == null) {
            DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
            ArticoloDAO articoloDAO = factory.getArticoloDAO();
            articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());

            if (articoloModel == null) {
                throw new IllegalArgumentException("Articolo non trovato.");
            }
            magazzino.aggiungiArticolo(articoloModel);
        }

        if (!magazzino.verificaDisponibilita(articoloModel.leggiId(), quantita)) {
            throw new QuantitaInsufficienteException(
                    "Quantità richiesta (" + quantita + ") non disponibile. Scorta residua: " + articoloModel.ottieniScorta()
            );
        }

        this.carrello.aggiungiArticolo(articoloModel, quantita);
    }

    public void rimuoviArticoloDalCarrello(ArticoloBean articoloBean) {
        Map<Articolo, Integer> articoliNelCarrello = this.carrello.getListaArticoli();
        Articolo articoloDaRimuovere = null;

        for (Articolo a : articoliNelCarrello.keySet()) {
            if (a.leggiId() == articoloBean.getId()) {
                articoloDaRimuovere = a;
                break;
            }
        }

        if (articoloDaRimuovere != null) {
            this.carrello.rimuoviArticolo(articoloDaRimuovere);
        }
    }

    public CarrelloBean visualizzaCarrello() {
        CarrelloBean cb = new CarrelloBean();
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            ArticoloBean b = convertiModelInBean(entry.getKey());
            // Imposto la quantità selezionata dall'utente nel carrello
            b.setQuantita(entry.getValue());
            cb.aggiungiArticolo(b);
        }
        cb.setTotale(this.carrello.calcolaTotale());
        return cb;
    }

    // -----------------------------------------------------------------
    // GESTIONE UTENTE E PAGAMENTO
    // -----------------------------------------------------------------

    public UtenteBean recuperaDatiCliente(int idCliente) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        UtenteDAO utenteDAO = factory.getUtenteDAO();
        Utente utente = utenteDAO.findById(idCliente);

        UtenteBean bean = new UtenteBean();
        if (utente != null) {
            bean.setUsername(utente.leggiUsername());
            bean.setEmail(utente.leggiEmail());
            bean.setIndirizzo(utente.leggiIndirizzo());
            bean.setRuolo(utente.scopriRuolo());
        }
        return bean;
    }

    public OrdineBean completaAcquisto(PagamentoBean datiPagamento, CarrelloBean carrelloBean, UtenteBean utenteBean)
            throws DAOException, PaymentException {

        if (datiPagamento == null) throw new PaymentException("Dati pagamento mancanti.");
        if (Math.abs(datiPagamento.getImportoDaPagare() - this.carrello.calcolaTotale()) > 0.01) {
            throw new PaymentException("Errore importo: Totale carrello modificato durante il pagamento.");
        }

        Session session = SessionManager.getInstance().getSession(this.sessionId);
        if (session == null) throw new IllegalStateException("Sessione scaduta.");
        Utente utenteCorrente = session.getUtenteCorrente();

        // Creazione Entity Ordine
        Ordine ordine = new Ordine(0, new Date(), utenteCorrente, this.carrello.getListaArticoli(), this.carrello.calcolaTotale());
        ordine.setStato("IN_ATTESA");

        // Persistenza DB
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();

        // Salva ordine e righe ordine
        ordineDAO.insertOrdine(ordine);

        // Aggiorna magazzino
        Magazzino magazzino = Magazzino.getInstance();
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            Articolo art = entry.getKey();
            int qtaAcquistata = entry.getValue();
            magazzino.scaricaMerce(art.leggiId(), qtaAcquistata);
            articoloDAO.updateScorta(art);
        }

        // Notifica Commesso (Observer)
        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordine);

        // Aggiorna sessione
        session.setUltimoOrdineCreato(ordine);
        this.carrello.svuota();

        return convertiOrdineInBean(ordine);
    }

    // -----------------------------------------------------------------
    // GESTIONE ORDINI LATO COMMESSO E FLUSSO NOTIFICHE
    // -----------------------------------------------------------------

    /**
     * Recupera tutti gli ordini per il commesso.
     */
    public List<OrdineBean> recuperaOrdiniPendenti() throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();

        List<Ordine> ordini = ordineDAO.findAll();
        List<OrdineBean> beans = new ArrayList<>();

        for (Ordine o : ordini) {
            beans.add(convertiOrdineInBean(o));
        }
        return beans;
    }

    /**
     * Aggiorna lo stato dell'ordine a "CLIENTE_IN_NEGOZIO" e notifica il commesso.
     */
    public void segnalaClienteInNegozio(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();

        // 1. Aggiornamento DB per persistenza
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);
        if (ordine != null) {
            ordine.setStato("CLIENTE_IN_NEGOZIO");
            ordineDAO.updateStato(ordine);
        }

        // 2. Invio Notifica (Observer)
        // Il messaggio contiene il codice "CLIENTE_IN_NEGOZIO" che il CommessoGraphicController ascolta
        GestoreNotifiche.getInstance().inviaMessaggio("CLIENTE_IN_NEGOZIO: Ordine #" + idOrdine);
    }

    /**
     * Il commesso conferma che la merce è pronta.
     * Aggiorna stato a "PRONTO" e notifica il cliente.
     */
    public void confermaRitiroMerce(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();

        // 1. Aggiornamento DB
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);
        if (ordine != null) {
            ordine.setStato("PRONTO"); // o "EVASO" a seconda delle tue preferenze
            ordineDAO.updateStato(ordine);

            // 2. Notifica Observer (ascoltata dal Cliente se in attesa)
            GestoreNotifiche.getInstance().inviaMessaggio("MERCE_PRONTA: Ordine #" + idOrdine);
        }
    }

    // -----------------------------------------------------------------
    // CONVERTITORI (PRIVATE)
    // -----------------------------------------------------------------

    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean b = new ArticoloBean();
        b.setId(a.leggiId());
        b.setDescrizione(a.leggiDescrizione());
        b.setPrezzo(a.ottieniPrezzo());
        // Nota: Nel catalogo questa è la scorta. Nel carrello sovrascriviamo con la qta ordinata.
        b.setQuantita(a.ottieniScorta());
        b.setImmaginePath(a.getImmaginePath());

        if (a instanceof Mangime) {
            b.setType("MANGIME");
            b.setDataScadenza(((Mangime) a).getScadenza());
        } else if (a instanceof Utensile) {
            b.setType("UTENSILE");
            b.setMateriale(((Utensile) a).getMateriale());
        } else if (a instanceof Fitofarmaco) {
            b.setType("FITOFARMACO");
            b.setServePatentino(((Fitofarmaco) a).isRichiedePatentino());
        }
        return b;
    }

    private OrdineBean convertiOrdineInBean(Ordine o) {
        OrdineBean b = new OrdineBean();
        b.setId(o.leggiId());
        b.setDataCreazione(o.getDataCreazione());
        b.setTotale(o.getTotale());
        b.setStato(o.getStato());

        // CRUCIALE: Convertiamo anche la lista articoli dell'ordine
        // Questo serve per il dettaglio ordine lato Commesso
        List<ArticoloBean> listaArticoliBean = new ArrayList<>();

        if (o.getArticoli() != null) {
            // Nota: La mappa in Ordine è <Articolo, Quantità> o List<Articolo> (dipende dall'implementazione model)
            // Assumendo che Ordine.java abbia Map<Articolo, Integer> articoli o simile.
            // Se Ordine ha Map<Articolo, Integer>:
            for (Map.Entry<Articolo, Integer> entry : o.getArticoli().entrySet()) {
                ArticoloBean ab = convertiModelInBean(entry.getKey());
                ab.setQuantita(entry.getValue()); // Imposta la quantità comprata
                listaArticoliBean.add(ab);
            }
        }
        b.setArticoli(listaArticoliBean);

        return b;
    }
}