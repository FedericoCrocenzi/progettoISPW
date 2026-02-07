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
import it.ispw.project.exception.PaymentException;
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

    public void aggiungiArticoloAlCarrello(ArticoloBean articoloBean, int quantita) throws IllegalArgumentException, DAOException {
        if (quantita <= 0) throw new IllegalArgumentException("La quantità deve essere positiva.");

        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        Articolo articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());

        if (articoloModel == null) throw new IllegalArgumentException("Articolo non più esistente.");
        if (!articoloModel.checkDisponibilita(quantita)) throw new IllegalArgumentException("Quantità insufficiente.");

        this.carrello.aggiungiArticolo(articoloModel, quantita);
    }

    public OrdineBean completaAcquisto(PagamentoBean datiPagamento, CarrelloBean carrelloBean, UtenteBean utenteBean)
            throws DAOException, PaymentException {

        // 1. VALIDAZIONE PAGAMENTO
        if (datiPagamento == null) throw new PaymentException("Dati di pagamento mancanti.");

        double totaleAtteso = this.carrello.calcolaTotale();
        if (Math.abs(datiPagamento.getImportoDaPagare() - totaleAtteso) > 0.01) {
            throw new PaymentException("L'importo del pagamento non corrisponde al totale del carrello.");
        }

        String metodo = datiPagamento.getMetodoPagamento();
        if (metodo == null) throw new PaymentException("Selezionare un metodo di pagamento.");

        switch (metodo) {
            case "CARTA_CREDITO":
                if (datiPagamento.getNumeroCarta() == null || datiPagamento.getNumeroCarta().length() != 16)
                    throw new PaymentException("Numero carta non valido.");
                if (datiPagamento.getCvv() == null || datiPagamento.getCvv().length() != 3)
                    throw new PaymentException("CVV non valido.");
                if (datiPagamento.getDataScadenza() == null || !datiPagamento.getDataScadenza().contains("/"))
                    throw new PaymentException("Formato scadenza non valido.");
                break;
            case "PAYPAL": break;
            case "CONTANTI_CONSEGNA": break;
            default: throw new PaymentException("Metodo di pagamento non supportato.");
        }

        // 2. CONTROLLO PRE-ORDINE
        if (this.carrello.getListaArticoli().isEmpty()) throw new IllegalStateException("Il carrello è vuoto.");
        if (this.sessionId == null) throw new IllegalStateException("Sessione non valida.");

        Session session = SessionManager.getInstance().getSession(this.sessionId);
        if (session == null) throw new IllegalStateException("Sessione scaduta.");
        Utente utenteCorrente = session.getUtenteCorrente();

        // 3. CREAZIONE ORDINE
        Ordine ordine = new Ordine(0, new Date(), utenteCorrente, this.carrello.getListaArticoli(), this.carrello.calcolaTotale());

        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();

        ordineDAO.insertOrdine(ordine);

        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            Articolo art = entry.getKey();
            art.aggiornaScorta(-entry.getValue());
            articoloDAO.updateScorta(art);
        }

        // 4. NOTIFICA INIZIALE (Nuovo Ordine)
        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordine);

        // 5. AGGIORNAMENTO SESSIONE
        session.setUltimoOrdineCreato(ordine);

        // 6. PULIZIA
        this.carrello.svuota();

        return convertiOrdineInBean(ordine);
    }

    // --- METODI DI NOTIFICA AGGIORNATI ---

    /**
     * Chiamato dalla view CLIENTE quando clicca "Sono in negozio".
     * Usa il pattern Observer per avvisare il Commesso.
     */
    public void notificaPresenzaInNegozio(int idOrdine) {
        // Messaggio strutturato che il NotificaGraphicController (Commesso) intercetterà
        String messaggio = "CLIENTE_IN_NEGOZIO: Ordine #" + idOrdine;
        GestoreNotifiche.getInstance().inviaMessaggio(messaggio);
    }

    /**
     * Chiamato dalla view COMMESSO quando clicca "Merce Pronta".
     * Usa il pattern Observer per avvisare il Cliente.
     */
    public void confermaRitiroMerce(int idOrdine) {
        // Qui potresti anche aggiornare lo stato dell'ordine nel DB (es. "PRONTO")
        // DAOFactory.getDAOFactory(...).getOrdineDAO().updateStato(idOrdine, "PRONTO");

        String messaggio = "MERCE_PRONTA: Ordine #" + idOrdine;
        GestoreNotifiche.getInstance().inviaMessaggio(messaggio);
    }

    // -----------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------

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

    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean bean = new ArticoloBean();
        bean.setId(a.leggiId());
        bean.setDescrizione(a.leggiDescrizione());
        bean.setPrezzo(a.ottieniPrezzo());
        bean.setQuantita(a.ottieniScorta());
        if (a instanceof Mangime) {
            bean.setType("MANGIME");
            bean.setDataScadenza(((Mangime) a).getScadenza());
        } else if (a instanceof Utensile) {
            bean.setType("UTENSILE");
            bean.setMateriale(((Utensile) a).getMateriale());
        } else if (a instanceof Fitofarmaco) {
            bean.setType("FITOFARMACO");
            bean.setServePatentino(((Fitofarmaco) a).isRichiedePatentino());
        }
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