package it.ispw.project.application_controller;

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
import it.ispw.project.session_manager.Session;
import it.ispw.project.session_manager.SessionManager;
import it.ispw.project.validation.PagamentoValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AcquistaArticoloControllerApplicativo {

    private static final String STATO_ORDINE_IN_ELABORAZIONE = "IN_ELABORAZIONE";

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

    public List<ArticoloBean> visualizzaCatalogo() {
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

    public List<ArticoloBean> ricercaArticoli(RicercaArticoloBean criteri) {
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
            throws QuantitaInsufficienteException {

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

        int quantitaTotaleRichiesta = calcolaQuantitaNelCarrello(carrello, articoloModel.leggiId()) + quantita;
        if (!magazzino.verificaDisponibilita(articoloModel.leggiId(), quantitaTotaleRichiesta)) {
            throw new QuantitaInsufficienteException(
                    "Quantità non disponibile. Scorta: " + articoloModel.ottieniScorta()
            );
        }

        carrello.aggiungiArticolo(articoloModel, quantita);
    }

    private int calcolaQuantitaNelCarrello(Carrello carrello, int idArticolo) {
        int quantita = 0;
        for (Map.Entry<Articolo, Integer> entry : carrello.getListaArticoli().entrySet()) {
            if (entry.getKey().leggiId() == idArticolo) {
                quantita += entry.getValue();
            }
        }
        return quantita;
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

    public void diminuisciQuantitaArticoloDalCarrello(String sessionId, ArticoloBean articoloBean, int quantita) {
        if (articoloBean == null || quantita <= 0) {
            throw new IllegalArgumentException("Quantita non valida.");
        }

        Carrello carrello = recuperaCarrelloDaSessione(sessionId);

        Articolo articoloDaDiminuire = null;
        for (Articolo a : carrello.getListaArticoli().keySet()) {
            if (a.leggiId() == articoloBean.getId()) {
                articoloDaDiminuire = a;
                break;
            }
        }

        if (articoloDaDiminuire != null) {
            carrello.diminuisciQuantita(articoloDaDiminuire, quantita);
        }
    }

    public CarrelloBean visualizzaCarrello(String sessionId) {
        Carrello carrello = recuperaCarrelloDaSessione(sessionId);

        CarrelloBean cb = new CarrelloBean();
        for (Map.Entry<Articolo, Integer> entry : carrello.getListaArticoli().entrySet()) {
            cb.aggiungiArticolo(convertiArticoloConQuantitaInBean(entry));
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

        return convertiUtenteInBean(utente);
    }

    public OrdineBean completaAcquisto(String sessionId, PagamentoBean datiPagamento)
            throws DAOException, PaymentException {

        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session == null)
            throw new IllegalStateException("Sessione scaduta.");

        Carrello carrello = session.getCarrelloCorrente();
        Utente utenteCorrente = session.getUtenteCorrente();

        if (carrello.getListaArticoli().isEmpty()) {
            throw new PaymentException("Il carrello è vuoto.");
        }

        if (datiPagamento == null)
            throw new PaymentException("Dati pagamento mancanti.");

        PagamentoValidator.valida(datiPagamento);

        if (Math.abs(datiPagamento.getImportoDaPagare() - carrello.calcolaTotale()) > 0.01)
            throw new PaymentException("Errore importo: il totale è cambiato.");

        Ordine ordine = new Ordine(
                0,
                new Date(),
                utenteCorrente,
                new HashMap<>(carrello.getListaArticoli()),
                carrello.calcolaTotale()
        );
        ordine.setStato(STATO_ORDINE_IN_ELABORAZIONE);

        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        Magazzino magazzino = Magazzino.getInstance();

        if (!magazzino.verificaCoperturaOrdine(ordine)) {
            throw new PaymentException("Scorte insufficienti per completare l'acquisto.");
        }

        ordineDAO.insertOrdine(ordine);
        magazzino.scaricaMerceOrdine(ordine);

        // Il Magazzino applica la regola di dominio; il DAO persiste lo stato risultante.
        List<Articolo> articoliPersistiti = new ArrayList<>();
        for (Articolo art : ordine.getArticoli().keySet()) {
            boolean scortaAggiornata;
            try {
                scortaAggiornata = articoloDAO.updateScorta(art);
            } catch (RuntimeException e) {
                magazzino.ripristinaMerceOrdine(ordine);
                ripristinaScortePersistite(articoloDAO, articoliPersistiti);
                throw new DAOException("Errore durante l'aggiornamento delle scorte.", e);
            }

            if (!scortaAggiornata) {
                magazzino.ripristinaMerceOrdine(ordine);
                ripristinaScortePersistite(articoloDAO, articoliPersistiti);
                throw new DAOException("Aggiornamento scorte non riuscito. Acquisto annullato.");
            }
            articoliPersistiti.add(art);
        }

        OrdineBean ordineBean = convertiOrdineInBean(ordine);
        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordineBean);
        session.setUltimoOrdineCreato(ordine);
        carrello.svuota();

        return ordineBean;
    }

    private void ripristinaScortePersistite(ArticoloDAO articoloDAO, List<Articolo> articoliPersistiti)
            throws DAOException {
        for (Articolo art : articoliPersistiti) {
            if (!articoloDAO.updateScorta(art)) {
                throw new DAOException("Rollback persistente delle scorte non riuscito.");
            }
        }
    }

    // -----------------------------------------------------------------
    // METODI COMMESSO
    // -----------------------------------------------------------------

    public List<OrdineBean> recuperaOrdiniPendenti() throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        List<Ordine> ordini = ordineDAO.findByStato(STATO_ORDINE_IN_ELABORAZIONE);

        List<OrdineBean> beans = new ArrayList<>();
        for (Ordine o : ordini) {
            beans.add(convertiOrdineInBean(o));
        }
        return beans;
    }

   /* public void segnalaClienteInNegozio(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);

        if (ordine != null) {
            ordine.setStato("CLIENTE_IN_NEGOZIO");
            ordineDAO.updateStato(ordine);
            GestoreNotifiche.getInstance()
                    .inviaMessaggio("CLIENTE_IN_NEGOZIO: Ordine #" + idOrdine);
        }
    }*/

    public void confermaRitiroMerce(int idOrdine) throws DAOException {
        DAOFactory factory = DAOFactory.getDAOFactory();
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        Ordine ordine = ordineDAO.selectOrdineById(idOrdine);

        if (ordine == null || !STATO_ORDINE_IN_ELABORAZIONE.equals(ordine.getStato())) {
            return;
        }

        ordine.setStato("PRONTO");
        ordineDAO.updateStato(ordine);
        GestoreNotifiche.getInstance().inviaNotificaMercePronta(creaNotificaOrdine(ordine));
    }

    public List<NotificaOrdineBean> recuperaNotificheMercePronta(String sessionId) {
        Session session = SessionManager.getInstance().getSession(sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return GestoreNotifiche.getInstance().getNotificheMerceProntaPerCliente(session.getUserId());
    }

    public boolean notificaDestinataAllaSessione(String sessionId, NotificaOrdineBean notifica) {
        Session session = SessionManager.getInstance().getSession(sessionId);
        return session != null
                && notifica != null
                && session.getUserId() == notifica.getIdCliente();
    }

    public void confermaLetturaNotificaMercePronta(int idOrdine) {
        GestoreNotifiche.getInstance().rimuoviNotificaMercePronta(idOrdine);
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

        if (a instanceof Mangime mangime) {
            b.setType("MANGIME");
            b.setDataScadenza(mangime.getScadenza());
        } else if (a instanceof Utensile utensile) {
            b.setType("UTENSILE");
            b.setMateriale(utensile.getMateriale());
        } else if (a instanceof Fitofarmaco fitofarmaco) {
            b.setType("FITOFARMACO");
            b.setServePatentino(fitofarmaco.isRichiedePatentino());
        }
        return b;
    }

    private ArticoloBean convertiArticoloConQuantitaInBean(Map.Entry<Articolo, Integer> entry) {
        ArticoloBean articoloBean = convertiModelInBean(entry.getKey());
        articoloBean.setQuantita(entry.getValue());
        return articoloBean;
    }

    private UtenteBean convertiUtenteInBean(Utente utente) {
        UtenteBean bean = new UtenteBean();
        if (utente != null) {
            bean.setUsername(utente.leggiUsername());
            bean.setEmail(utente.leggiEmail());
            bean.setIndirizzo(utente.leggiIndirizzo());
            bean.setRuolo(utente.scopriRuolo());
        }
        return bean;
    }

    private OrdineBean convertiOrdineInBean(Ordine o) {
        OrdineBean b = new OrdineBean();
        b.setId(o.leggiId());
        b.setDataCreazione(o.getDataCreazione());
        b.setTotale(o.getTotale());
        b.setStato(o.getStato());

        if (o.getCliente() != null) {
            UtenteBean clienteBean = convertiUtenteInBean(o.getCliente());
            clienteBean.setId(o.getCliente().ottieniId());
            b.setCliente(clienteBean);
        }

        List<ArticoloBean> articoliBean = new ArrayList<>();
        if (o.getArticoli() != null) {
            for (Map.Entry<Articolo, Integer> entry : o.getArticoli().entrySet()) {
                articoliBean.add(convertiArticoloConQuantitaInBean(entry));
            }
        }
        b.setArticoli(articoliBean);
        return b;
    }

    private NotificaOrdineBean creaNotificaOrdine(Ordine ordine) {
        NotificaOrdineBean notifica = new NotificaOrdineBean();
        notifica.setIdOrdine(ordine.leggiId());
        notifica.setStato(ordine.getStato());
        if (ordine.getCliente() != null) {
            notifica.setIdCliente(ordine.getCliente().ottieniId());
        }
        notifica.setOrdine(convertiOrdineInBean(ordine));
        return notifica;
    }
}
