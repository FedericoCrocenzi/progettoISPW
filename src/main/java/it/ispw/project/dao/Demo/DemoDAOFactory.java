package it.ispw.project.dao.Demo;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;

public class DemoDAOFactory extends DAOFactory {

    @Override
    public ArticoloDAO getArticoloDAO() {
        return new MemoryArticoloDAO(); // Dati scritti hardcoded nel codice
    }

    @Override
    public OrdineDAO getOrdineDAO() {
        return new MemoryOrdineDAO(); // Salva in una List temporanea
    }

    @Override
    public UtenteDAO getUtenteDAO() {
        return new MemoryUtenteDAO();
    }
}