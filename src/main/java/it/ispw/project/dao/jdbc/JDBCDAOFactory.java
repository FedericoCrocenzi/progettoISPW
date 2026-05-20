package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;

public class JDBCDAOFactory extends DAOFactory {

    private static final ArticoloDAO ARTICOLO_DAO = new JDBCArticoloDAO();
    private static final UtenteDAO UTENTE_DAO = new JDBCUtenteDAO();
    private static final OrdineDAO ORDINE_DAO = new JDBCOrdineDAO(UTENTE_DAO, ARTICOLO_DAO);

    static ArticoloDAO getArticoloDAOCondiviso() {
        return ARTICOLO_DAO;
    }

    static UtenteDAO getUtenteDAOCondiviso() {
        return UTENTE_DAO;
    }

    @Override
    public ArticoloDAO getArticoloDAO() {
        return ARTICOLO_DAO;
    }

    @Override
    public OrdineDAO getOrdineDAO() {
        return ORDINE_DAO;
    }

    @Override
    public UtenteDAO getUtenteDAO() {
        return UTENTE_DAO;
    }
}
