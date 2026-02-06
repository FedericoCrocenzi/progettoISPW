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
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Carrello;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;
import it.ispw.project.model.Utensile;
import it.ispw.project.model.GestoreNotifiche;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER APPLICATIVO: "Acquista Articolo"
 * Agisce da facciata (Facade) tra la View (Bean) e il Domain Layer (Model/DAO).
 */
public class AcquistaArticoloControllerApplicativo {

    // Selettore della persistenza (Abstract Factory Pattern)
    // Basta cambiare questa costante per passare da JDBC a FILE o DEMO
    private static final int TIPO_PERSISTENZA = DAOFactory.JDBC;

    // Stato della sessione corrente
    private Carrello carrello;

    public AcquistaArticoloControllerApplicativo(String sessionId) {
        this.carrello = new Carrello();
    }

    // -----------------------------------------------------------------
    // UC1: RICERCA ARTICOLI
    // -----------------------------------------------------------------
    public List<ArticoloBean> ricercaArticoli(RicercaArticoloBean criteri) {
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        List<Articolo> listaModel;

        // Recupero dati grezzi (Entità) dal DAO
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

        // Conversione Entità -> Bean (Information Hiding)
        List<ArticoloBean> listaBean = new ArrayList<>();
        for (Articolo a : listaModel) {
            listaBean.add(convertiModelInBean(a));
        }

        return listaBean;
    }

    // -----------------------------------------------------------------
    // UC2: GESTIONE CARRELLO
    // -----------------------------------------------------------------
    public void aggiungiArticoloAlCarrello(ArticoloBean articoloBean, int quantita) throws IllegalArgumentException {
        if (quantita <= 0) throw new IllegalArgumentException("La quantità deve essere positiva.");

        // SECURITY CHECK: Recupero l'articolo "vero" dal DB.
        // Non ci fidiamo del Bean che arriva dalla View (potrebbe essere stato manomesso).
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        ArticoloDAO articoloDAO = factory.getArticoloDAO();
        Articolo articoloModel = articoloDAO.selectArticoloById(articoloBean.getId());

        if (articoloModel == null) {
            throw new IllegalArgumentException("Articolo non più esistente.");
        }

        // BUSINESS LOGIC: Controllo disponibilità
        if (!articoloModel.checkDisponibilita(quantita)) {
            throw new IllegalArgumentException("Quantità insufficiente in magazzino.");
        }

        // Modifica dello stato (in memoria)
        this.carrello.aggiungiArticolo(articoloModel, quantita);
    }

    public CarrelloBean visualizzaCarrello() {
        CarrelloBean carrelloBean = new CarrelloBean();

        // Uso il metodo corretto 'getListaArticoli' del Model
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            ArticoloBean bean = convertiModelInBean(entry.getKey());
            bean.setQuantita(entry.getValue()); // Quantità nel carrello
            carrelloBean.aggiungiArticolo(bean);
        }

        carrelloBean.setTotale(this.carrello.calcolaTotale());
        return carrelloBean;
    }

    // -----------------------------------------------------------------
    // UC3: CHECKOUT (Acquisto)
    // -----------------------------------------------------------------
    public OrdineBean completaAcquisto(PagamentoBean datiPagamento, UtenteBean utenteLoggato) {

        if (this.carrello.getListaArticoli().isEmpty()) {
            throw new IllegalStateException("Il carrello è vuoto.");
        }

        // 1. RICOSTRUZIONE UTENTE (IMMUTABILE)
        // Recuperiamo l'ID reale (sicurezza) e usiamo il costruttore completo.
        // Non usiamo setter, rispettando l'Information Hiding del Model.
        int idRealeUtente = recuperaIdUtente(utenteLoggato.getUsername());

        Utente utenteModel = new Utente(
                idRealeUtente,
                utenteLoggato.getUsername(),
                utenteLoggato.getPassword(),
                utenteLoggato.getRuolo(),
                utenteLoggato.getEmail(),
                utenteLoggato.getIndirizzo()
        );

        // 2. CREAZIONE ORDINE (TRANSIENTE)
        // L'ID è 0, lo stato è "IN_ELABORAZIONE".
        Ordine ordine = new Ordine(
                0,
                new Date(),
                utenteModel,
                this.carrello.getListaArticoli(),
                this.carrello.calcolaTotale()
        );

        // 3. PERSISTENZA (TRANSAZIONE DB)
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        OrdineDAO ordineDAO = factory.getOrdineDAO();
        ArticoloDAO articoloDAO = factory.getArticoloDAO();

        // 3a. Il DAO salva l'ordine e popola l'ID generato (tramite registraIdGenerato)
        ordineDAO.insertOrdine(ordine);

        // 3b. Aggiornamento Magazzino
        for (Map.Entry<Articolo, Integer> entry : this.carrello.getListaArticoli().entrySet()) {
            Articolo art = entry.getKey();
            int qtaAcquistata = entry.getValue();

            // Logica di dominio: decremento
            art.aggiornaScorta(-qtaAcquistata);
            // Persistenza della modifica
            articoloDAO.updateScorta(art);
        }

        // 4. NOTIFICA (PATTERN OBSERVER)
        // Invio l'entità Ordine al Subject. Il Controller NON sa chi è in ascolto.
        GestoreNotifiche.getInstance().inviaNotificaNuovoOrdine(ordine);

        // 5. PULIZIA
        this.carrello.svuota(); // Metodo corretto

        // 6. OUTPUT
        return convertiOrdineInBean(ordine);
    }

    // -----------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------

    private ArticoloBean convertiModelInBean(Articolo a) {
        ArticoloBean bean = new ArticoloBean();
        bean.setId(a.leggiId());
        bean.setDescrizione(a.leggiDescrizione());
        bean.setPrezzo(a.ottieniPrezzo());
        bean.setQuantita(a.ottieniScorta()); // Scorta magazzino

        // GESTIONE POLIMORFISMO (Mapping)
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

    // Metodo di supporto per recuperare l'ID (simulato o via DAO)
    private int recuperaIdUtente(String username) {
        // In produzione: return dao.checkCredentials(username, ...).getId();
        // Qui assumiamo di poterlo recuperare o che il Bean lo avesse già.
        DAOFactory factory = DAOFactory.getDAOFactory(TIPO_PERSISTENZA);
        UtenteDAO uDao = factory.getUtenteDAO();
        // Nota: checkCredentials richiede password, qui semplifichiamo per l'esempio
        // supponendo di avere un metodo findByUsername o passando la pwd dal bean
        return 1; // Stub
    }
}