package it.ispw.project.applicationController;

import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.CarrelloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.bean.PagamentoBean;
import it.ispw.project.bean.RicercaArticoloBean;
import it.ispw.project.bean.UtenteBean;
import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Carrello;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;
import it.ispw.project.model.Utensile;
import it.ispw.project.model.GestoreNotifiche;
import it.ispw.project.sessionManager.Session;
import it.ispw.project.sessionManager.SessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AcquistaArticoloControllerApplicativo {

    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;
    private Carrello carrello;
    private String sessionId; // Manteniamo riferimento alla sessione

    // --- MODIFICA FONDAMENTALE ---
    // Il costruttore ora accetta il sessionId
    public AcquistaArticoloControllerApplicativo(String sessionId) {
        this.sessionId = sessionId;

        // Recuperiamo il carrello dalla Sessione invece di crearne uno nuovo!
        // Questo allinea Controller e View sugli stessi dati.
        this.carrello = SessionManager.getInstance().getSession(sessionId).getCarrelloCorrente();
    }

    // Costruttore di default (se serve per test, ma crea un carrello isolato)
    public AcquistaArticoloControllerApplicativo() {
        this.carrello = new Carrello();
    }

    // ... visualizzaCatalogo, recuperaDatiCliente, ricercaArticoli restano uguali ...
    public List<ArticoloBean> visualizzaCatalogo() throws DAOException {
        return ricercaArticoli(null);
    }

    public UtenteBean recuperaDatiCliente(int idCliente) throws DAOException {
        // ... (codice invariato)
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
        // ... (codice invariato)
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        List<Articolo> listaModel;
        if (criteri == null) listaModel = articoloDAO.selectAllArticoli();
        else listaModel = articoloDAO.selectByFilter(criteri.getTestoRicerca(), criteri.getTipoArticolo(), criteri.getPrezzoMin(), criteri.getPrezzoMax());
        List<ArticoloBean> listaBean = new ArrayList<>();
        for (Articolo a : listaModel) listaBean.add(convertiModelInBean(a));
        return listaBean;
    }

    public void aggiungiArticoloAlCarrello(ArticoloBean articoloBean, int quantita) throws IllegalArgumentException, DAOException {
        // ... (codice invariato)
        if (quantita <= 0) throw new IllegalArgumentException("Quantità positiva richiesta.");
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        Articolo articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());
        if (articoloModel == null) throw new IllegalArgumentException("Articolo non esistente.");
        if (!articoloModel.checkDisponibilita(quantita)) throw new IllegalArgumentException("Quantità insufficiente.");

        // Questo 'this.carrello' ora punta a quello della Sessione
        this.carrello.aggiungiArticolo(articoloModel, quantita);
    }

    public OrdineBean completaAcquisto(PagamentoBean datiPagamento, CarrelloBean carrelloBean, UtenteBean utenteBean) throws DAOException {
        // Controllo stato interno
        if (this.carrello.getListaArticoli().isEmpty()) {
            throw new IllegalStateException("Il carrello è vuoto.");
        }

        // --- CORREZIONE QUI ---
        // Recuperiamo la sessione usando l'ID memorizzato nel controller.
        if (this.sessionId == null) {
            throw new IllegalStateException("Sessione non valida: impossibile identificare l'utente.");
        }

        Session session = SessionManager.getInstance().getSession(this.sessionId);
        if (session == null) {
            throw new IllegalStateException("Sessione scaduta o non trovata.");
        }

        // Ora il metodo getUtenteCorrente() esiste grazie alla modifica al punto 1
        Utente utenteCorrente = session.getUtenteCorrente();

        // Procedi con la creazione dell'ordine...
        Ordine ordine = new Ordine(
                0,
                new Date(),
                utenteCorrente,
                this.carrello.getListaArticoli(),
                this.carrello.calcolaTotale()
        );

        // ... (resto del metodo identico a prima: DAO, Notifiche, ecc.) ...
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        factory.getOrdineDAO().insertOrdine(ordine);
        // ...

        return convertiOrdineInBean(ordine);
    }

    public CarrelloBean visualizzaCarrello() {
        CarrelloBean carrelloBean = new CarrelloBean();
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            ArticoloBean bean = convertiModelInBean(entry.getKey());
            bean.setQuantita(entry.getValue());
            carrelloBean.aggiungiArticolo(bean);
        }
        carrelloBean.setTotale(this.carrello.calcolaTotale());
        return carrelloBean;
    }

    // ... helper convertiModelInBean e convertiOrdineInBean invariati ...
    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean bean = new ArticoloBean();
        bean.setId(a.leggiId());
        bean.setDescrizione(a.leggiDescrizione());
        bean.setPrezzo(a.ottieniPrezzo());
        bean.setQuantita(a.ottieniScorta());
        // ... gestione tipi ...
        return bean;
    }
    private OrdineBean convertiOrdineInBean(Ordine o) {
        OrdineBean bean = new OrdineBean();
        bean.setId(o.leggiId());
        bean.setDataCreazione(o.getDataCreazione());
        bean.setTotale(o.getTotale());
        bean.setStato(o.getStato());
        return bean;
    }
}