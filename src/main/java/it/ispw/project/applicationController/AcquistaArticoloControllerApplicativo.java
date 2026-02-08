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
import it.ispw.project.model.observer.Observer;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class AcquistaArticoloControllerApplicativo {

    public AcquistaArticoloControllerApplicativo() {
        // Costruttore vuoto (Stateless)
    }

    // -----------------------------------------------------------------
    // GESTIONE OBSERVER
    // -----------------------------------------------------------------

    public void registraOsservatoreCarrello(String sessionId, Observer observer) {
        Carrello carrello = recuperaCarrelloDaSessione(sessionId);
        if (carrello != null) {
            carrello.attach(observer);
        }
    }

    public void rimuoviOsservatoreCarrello(String sessionId, Observer observer) {
        Carrello carrello = recuperaCarrelloDaSessione(sessionId);
        if (carrello != null) {
            carrello.detach(observer);
        }
    }

    // -----------------------------------------------------------------
    // GESTIONE CATALOGO
    // -----------------------------------------------------------------

    public List<ArticoloBean> visualizzaCatalogo() throws DAOException {
        Magazzino magazzino = Magazzino.getInstance();

        // Lazy loading del catalogo
        if (magazzino.getCatalogo().isEmpty()) {
            DAOFactory factory = DAOFactory.getDAOFactory();
            ArticoloDAO articoloDAO = factory.getArticoloDAO();
            List<Articolo> articoliDB = articoloDAO.selectAllArticoli();

            for (Articolo a : articoliDB) {
                magazzino.aggiungiArticolo(a);
            }
        }

        List<ArticoloBean> listaBean = new ArrayList<>();
        for (Articolo a : magazzino.getCatalogo().values()) {
            listaBean.add(convertiModelInBean(a));
        }
        return listaBean;
    }

    public List<ArticoloBean> ricercaArticoli(RicercaArticoloBean criteri) throws DAOException {
        if (criteri == null ||
                (criteri.getTestoRicerca() == null && criteri.getTipoArticolo() == null)) {
            return visualizzaCatalogo();
        }

        DAOFactory factory = DAOFactory.getDAOFactory();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();

        List<Articolo> listaModel = articoloDAO.selectByFilter(
                criteri.getTestoRicerca(),
                criteri.getTipoArticolo(),
                criteri.getPrezzoMin(),
                criteri.getPrezzoMax()
        );

        List<ArticoloBean> listaBean = new ArrayList<>();
        Magazzino magazzino = Magazzino.getInstance();

        for (Articolo a : listaModel) {
            magazzino.aggiungiArticolo(a);
            listaBean.add(convertiModelInBean(a));
        }
        return listaBean;
    }

    // -----------------------------------------------------------------
    // GESTIONE CARRELLO
    // -----------------------------------------------------------------

    public void aggiungiArticoloAlCarrello(String sessionId, ArticoloBean articoloBean, int quantita)
            throws IllegalArgumentException, DAOException, QuantitaInsufficienteException {

        if (quantita <= 0)
            throw new IllegalArgumentException("La quantità deve essere positiva.");

        Carrello carrello = recuperaCarrelloDaSessione(sessionId);
        Magazzino magazzino = Magazzino.getInstance();

        Articolo articoloModel = magazzino.trovaArticolo(articoloBean.getId());

        if (articoloModel == null) {
            DAOFactory factory = DAOFactory.getDAOFactory();
            ArticoloDAO articoloDAO = factory.getArticoloDAO();
            articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());

            if (articoloModel == null) {
                throw new IllegalArgumentException("Articolo non trovato.");
            }
            magazzino.aggiungiArticolo(articoloModel);
        }

        if (!magazzino.verificaDisponibilita(articoloModel.leggiId(), quantita)) {
            throw new QuantitaInsufficienteException(
                    "Quantità non disponibile. Scorta: " + articoloModel.ottieniScorta()
            );
        }

        carrello.aggiungiArticolo(articoloModel, quantita);
    }

    public void rimuoviArticoloDalCarrello(String sessionId, ArticoloBean articoloBean) {
        Carrello carrello = recuperaCarrelloDaSessione(sessionId);

        Articolo articoloDaRimuovere = null;
        for (Articolo a : carrello.getListaArticoli().keySet()) {
            if (a.leggiId() == articoloBean.getId()) {
                articoloDaRimuovere = a;
                break;
            }
        }

        if (articoloDaRimuovere != null) {
            carrello.rimuoviArticolo(articoloDaRimuovere);
        }
    }

    public CarrelloBean visualizzaCarrello(String sessionId) {
        Carrello carrello = recuperaCarrelloDaSessione(sessionId);

        CarrelloBean cb = new CarrelloBean();
        for (Map.Entry<Articolo, Integer> entry : carrello.getListaArticoli().entrySet()) {
            ArticoloBean b = convertiModelInBean(entry.getKey());
            b.setQuantita(entry.getValue());
            cb.aggiungiArticolo(b);
        }
        cb.setTotale(carrello.calcolaTotale());
        return cb;
    }

    // -----------------------------------------------------------------
    // GESTIONE ACQUISTO
    // -----------------------------------------------------------------

    public UtenteBean recuperaDatiCliente(int idCliente) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
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

    public OrdineBean completaAcquisto(String sessionId, PagamentoBean datiPagamento)
            throws DAOException, PaymentException {

        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session == null)
            throw new IllegalStateException("Sessione scaduta.");

        Carrello carrello = session.getCarrelloCorrente();
        Utente utenteCorrente = session.getUtenteCorrente();

        if (datiPagamento == null)
            throw new PaymentException("Dati pagamento mancanti.");

        if (Math.abs(datiPagamento.getImportoDaPagare() - carrello.calcolaTotale()) > 0.01)
            throw new PaymentException("Errore importo: il totale è cambiato.");

        Ordine ordine = new Ordine(
                0,
                new Date(),
                utenteCorrente,
                carrello.getListaArticoli(),
                carrello.calcolaTotale()
        );
        ordine.setStato("IN_ATTESA");

        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        Magazzino magazzino = Magazzino.getInstance();

        ordineDAO.insertOrdine(ordine);

        for (Map.Entry<Articolo, Integer> entry : carrello.getListaArticoli().entrySet()) {
            Articolo art = entry.getKey();
            int quantitaAcquistata = entry.getValue();

            magazzino.scaricaMerce(art.leggiId(), quantitaAcquistata);
            articoloDAO.updateScorta(art);
        }

        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordine);
        session.setUltimoOrdineCreato(ordine);
        carrello.svuota();

        return convertiOrdineInBean(ordine);
    }

    // -----------------------------------------------------------------
    // METODI COMMESSO
    // -----------------------------------------------------------------

    public List<OrdineBean> recuperaOrdiniPendenti() throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        List<Ordine> ordini = ordineDAO.findAll();

        List<OrdineBean> beans = new ArrayList<>();
        for (Ordine o : ordini) {
            beans.add(convertiOrdineInBean(o));
        }
        return beans;
    }

    public void segnalaClienteInNegozio(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);

        if (ordine != null) {
            ordine.setStato("CLIENTE_IN_NEGOZIO");
            ordineDAO.updateStato(ordine);
            GestoreNotifiche.getInstance()
                    .inviaMessaggio("CLIENTE_IN_NEGOZIO: Ordine #" + idOrdine);
        }
    }

    public void confermaRitiroMerce(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);

        if (ordine != null) {
            ordine.setStato("PRONTO");
            ordineDAO.updateStato(ordine);
            GestoreNotifiche.getInstance()
                    .inviaMessaggio("MERCE_PRONTA: Ordine #" + idOrdine);
        }
    }

    // -----------------------------------------------------------------
    // METODI DI SUPPORTO
    // -----------------------------------------------------------------

    private Carrello recuperaCarrelloDaSessione(String sessionId) {
        Session session = SessionManager.getInstance().getSession(sessionId);
        return (session != null) ? session.getCarrelloCorrente() : new Carrello();
    }

    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean b = new ArticoloBean();
        b.setId(a.leggiId());
        b.setDescrizione(a.leggiDescrizione());
        b.setPrezzo(a.ottieniPrezzo());
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
        return b;
    }
}
