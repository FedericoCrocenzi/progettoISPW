package it.ispw.project.dao.FileSystem;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.dao.UtenteDAO;

public class FileSystemDAOFactory extends DAOFactory {

    @Override
    public ArticoloDAO getArticoloDAO() {
        return new FileSystemArticoloDAO();
    }

    @Override
    public OrdineDAO getOrdineDAO() {
        return new FileSystemOrdineDAO();
    }

    @Override
    public UtenteDAO getUtenteDAO() {
        return new FileSystemUtenteDAO();
    }
}