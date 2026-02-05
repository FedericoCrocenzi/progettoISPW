package it.ispw.project.dao.jdbc;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;

public class JDBCDAOFactory extends DAOFactory {

    @Override
    public ArticoloDAO getArticoloDAO() {
        return new JDBCArticoloDAO(); // La classe che abbiamo scritto prima
    }

    @Override
    public OrdineDAO getOrdineDAO() {
        return new JDBCOrdineDAO();
    }

    @Override
    public UtenteDAO getUtenteDAO() {
        return new JDBCUtenteDAO();
    }
}