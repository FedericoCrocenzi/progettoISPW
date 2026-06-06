package it.ispw.project.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DAOFactoryTest {

    @Test
    void factoryConfigurataCreaDaoNonNull() {
        DAOFactory factory = DAOFactory.getDAOFactory();

        assertNotNull(factory);
        assertNotNull(factory.getArticoloDAO());
        assertNotNull(factory.getOrdineDAO());
        assertNotNull(factory.getUtenteDAO());
    }
}
