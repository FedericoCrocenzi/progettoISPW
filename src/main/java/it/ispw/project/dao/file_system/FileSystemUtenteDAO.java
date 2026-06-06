package it.ispw.project.dao.file_system;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Utente;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSystemUtenteDAO implements UtenteDAO {

    private static final Logger LOGGER = Logger.getLogger(FileSystemUtenteDAO.class.getName());
    private static final String CSV_FILE_NAME = "utenti.csv";
    private static final String SEPARATOR = ";";

    public FileSystemUtenteDAO() {
        File file = new File(CSV_FILE_NAME);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    LOGGER.warning("File utenti.csv non creato perche' gia' presente o non disponibile.");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Impossibile creare il file utenti.csv", e);
            }
        }
    }

    @Override
    public Utente checkCredentials(String identifier, String password) throws DAOException {
        File file = new File(CSV_FILE_NAME);
        String identifierNormalizzato = normalizza(identifier);
        String passwordNormalizzata = normalizza(password);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] dati = line.split(SEPARATOR, -1);

                if (dati.length >= 4) {
                    String userFile = normalizza(dati[1]);
                    String passFile = normalizza(dati[2]);
                    String emailFile = estraiCampoOpzionale(dati, 4);

                    boolean matchIdentifier = userFile.equals(identifierNormalizzato)
                            || (emailFile != null && emailFile.equals(identifierNormalizzato));

                    if (matchIdentifier && passFile.equals(passwordNormalizzata)) {
                        return parseUtente(dati);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new DAOException("Errore lettura file utenti: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Utente findById(int id) throws DAOException {
        File file = new File(CSV_FILE_NAME);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] dati = line.split(SEPARATOR, -1);
                validaFormatoRigaUtente(dati);
                if (idCorrisponde(dati, id)) {
                    return parseUtente(dati);
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore ricerca utente per ID su file", e);
        }
        return null;
    }

    private boolean idCorrisponde(String[] dati, int id) throws DAOException {
        try {
            return Integer.parseInt(dati[0]) == id;
        } catch (NumberFormatException e) {
            throw new DAOException("Riga utente con ID non valido nel file utenti.", e);
        }
    }

    private void validaFormatoRigaUtente(String[] dati) throws DAOException {
        if (dati.length < 4) {
            throw new DAOException("Riga utente con formato incompatibile nel file utenti.");
        }
    }

    private Utente parseUtente(String[] dati) {
        int id = Integer.parseInt(normalizza(dati[0]));
        String username = normalizza(dati[1]);
        String password = normalizza(dati[2]);
        String ruolo = normalizza(dati[3]);

        String email = estraiCampoOpzionale(dati, 4);
        String indirizzo = estraiCampoOpzionale(dati, 5);

        return new Utente(id, username, password, ruolo, email, indirizzo);
    }

    private String normalizza(String value) {
        return value == null ? "" : value.trim();
    }

    private String estraiCampoOpzionale(String[] dati, int indice) {
        if (dati.length <= indice) {
            return null;
        }

        String valore = normalizza(dati[indice]);
        return valore.isEmpty() || "null".equalsIgnoreCase(valore) ? null : valore;
    }
}
