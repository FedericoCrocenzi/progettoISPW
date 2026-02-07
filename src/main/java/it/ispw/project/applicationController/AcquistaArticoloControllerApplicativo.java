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
 * Gestisce la logica di carrello, pagamento, creazione ordine e notifiche.
 */
public class AcquistaArticoloControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;
    private Carrello carrello;
    private String sessionId;

    public AcquistaArticoloControllerApplicativo(String sessionId) {
        this.sessionId = sessionId;
        this.carrello = SessionManager.getInstance().getSession(sessionId).getCarrelloCorrente();
    }

    public AcquistaArticoloControllerApplicativo() {
        this.carrello = new Carrello();
    }

    // -----------------------------------------------------------------
    // METODI OPERATIVI
    // -----------------------------------------------------------------

    public List<ArticoloBean> visualizzaCatalogo() throws DAOException {
        return ricercaArticoli(null);
    }

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

        if (articoloModel == null) {
            DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
            ArticoloDAO articoloDAO = factory.getArticoloDAO();
            articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());

            if (articoloModel == null) {
                throw new IllegalArgumentException("Articolo non più esistente.");
            }
            magazzino.aggiungiArticolo(articoloModel);
        }

        if (!magazzino.verificaDisponibilita(articoloModel.leggiId(), quantita)) {
            throw new QuantitaInsufficienteException(
                    "Quantità richiesta (" + quantita + ") non disponibile in magazzino."
            );
        }

        this.carrello.aggiungiArticolo(articoloModel, quantita);
    }

    /**
     * NUOVO METODO: Rimuove un articolo dal carrello.
     * Cerca l'articolo nel carrello tramite ID e lo rimuove.
     */
    public void rimuoviArticoloDalCarrello(ArticoloBean articoloBean) {
        // Recupero la mappa degli articoli dal modello Carrello
        Map<Articolo, Integer> articoliNelCarrello = this.carrello.getListaArticoli();

        Articolo articoloDaRimuovere = null;

        // Itero per trovare l'oggetto Articolo (chiave) che corrisponde all'ID del Bean
        for (Articolo a : articoliNelCarrello.keySet()) {
            if (a.leggiId() == articoloBean.getId()) {
                articoloDaRimuovere = a;
                break;
            }
        }

        if (articoloDaRimuovere != null) {
            // Chiamo il metodo del modello per rimuovere (assicurati che Carrello.java lo abbia)
            this.carrello.rimuoviArticolo(articoloDaRimuovere);
        }
    }

    public OrdineBean completaAcquisto(PagamentoBean datiPagamento, CarrelloBean carrelloBean, UtenteBean utenteBean)
            throws DAOException, PaymentException {

        if (datiPagamento == null) throw new PaymentException("Dati di pagamento mancanti.");

        double totaleAtteso = this.carrello.calcolaTotale();
        if (Math.abs(datiPagamento.getImportoDaPagare() - totaleAtteso) > 0.01) {
            throw new PaymentException("Discrepanza totale.");
        }
        if (datiPagamento.getMetodoPagamento() == null) throw new PaymentException("Selezionare metodo.");

        if (this.carrello.getListaArticoli().isEmpty()) throw new IllegalStateException("Carrello vuoto.");
        if (this.sessionId == null) throw new IllegalStateException("Sessione non valida.");

        Session session = SessionManager.getInstance().getSession(this.sessionId);
        if (session == null) throw new IllegalStateException("Sessione scaduta.");
        Utente utenteCorrente = session.getUtenteCorrente();

        Ordine ordine = new Ordine(0, new Date(), utenteCorrente, this.carrello.getListaArticoli(), this.carrello.calcolaTotale());
        ordine.setStato("IN_ATTESA");

        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();

        ordineDAO.insertOrdine(ordine);

        Magazzino magazzino = Magazzino.getInstance();

        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            Articolo art = entry.getKey();
            int qtaAcquistata = entry.getValue();

            magazzino.scaricaMerce(art.leggiId(), qtaAcquistata);
            articoloDAO.updateScorta(art);
        }

        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordine);
        session.setUltimoOrdineCreato(ordine);
        this.carrello.svuota();

        return convertiOrdineInBean(ordine);
    }

    public List<OrdineBean> recuperaOrdiniPendenti() throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        List<Ordine> ordini = ordineDAO.findAll();
        List<OrdineBean> beans = new ArrayList<>();
        for (Ordine o : ordini) beans.add(convertiOrdineInBean(o));
        return beans;
    }

    public void notificaPresenzaInNegozio(int idOrdine) {
        GestoreNotifiche.getInstance().inviaMessaggio("CLIENTE_IN_NEGOZIO: Ordine #" + idOrdine);
    }

    public void confermaRitiroMerce(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);
        if (ordine != null) {
            ordine.setStato("PRONTO");
            ordineDAO.updateStato(ordine);
            GestoreNotifiche.getInstance().inviaMessaggio("MERCE_PRONTA: Ordine #" + idOrdine);
        }
    }

    public CarrelloBean visualizzaCarrello() {
        CarrelloBean cb = new CarrelloBean();
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            ArticoloBean b = convertiModelInBean(entry.getKey());
            b.setQuantita(entry.getValue());
            cb.aggiungiArticolo(b);
        }
        cb.setTotale(this.carrello.calcolaTotale());
        return cb;
    }

    // -----------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------

    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean b = new ArticoloBean();
        b.setId(a.leggiId());
        b.setDescrizione(a.leggiDescrizione());
        b.setPrezzo(a.ottieniPrezzo());
        b.setQuantita(a.ottieniScorta());

        // Trasferimento path immagine
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
        return b;
    }
}