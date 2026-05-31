package it.ispw.project.dao.fileSystem;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Utente;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSystemUtenteDAO implements UtenteDAO {

    private static final String CSV_FILE_NAME = "utenti.csv";
    private static final String SEPARATOR = ";";

    public FileSystemUtenteDAO() {
        // Assicuriamoci che il file esista, altrimenti lo creiamo
        File file = new File(CSV_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Impossibile creare il file utenti.csv");
            }
        }
    }

    /**
     * Verifica credenziali controllando sia USERNAME che EMAIL (Polimorfismo con JDBC).
     */
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

                // Controllo robustezza riga
                if (dati.length >= 4) {
                    String userFile = normalizza(dati[1]);
                    String passFile = normalizza(dati[2]);
                    String emailFile = estraiCampoOpzionale(dati, 4);

                    // Verifica: (Username == Input OR Email == Input) AND Password == Input
                    boolean matchIdentifier = userFile.equals(identifierNormalizzato) ||
                            (emailFile != null && emailFile.equals(identifierNormalizzato));

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

    /**
     * Recupera utente per ID.
     */
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
                if (dati.length >= 1) {
                    try {
                        int currentId = Integer.parseInt(dati[0]);
                        if (currentId == id) {
                            return parseUtente(dati);
                        }
                    } catch (NumberFormatException ignored) {
                        // Salta righe malformate
                    }
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore ricerca utente per ID su file", e);
        }
        return null;
    }

    /**
     * Salva un nuovo utente (Registrazione).
     * Calcola il nuovo ID leggendo l'ultimo ID presente nel file.
     */
    @Override
    public void salva(Utente utente) throws DAOException {
        File file = new File(CSV_FILE_NAME);
        int newId = calcolaNuovoId(file);

        // 2. Scrittura in coda (append = true)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            // Se il file non è vuoto, assicuriamoci di andare a capo prima di scrivere
            if (file.length() > 0) {
                bw.newLine();
            }

            bw.write(serializzaUtente(newId, utente));

        } catch (IOException e) {
            throw new DAOException("Errore salvataggio utente su file", e);
        }
    }

    // --- Helper Privato per evitare duplicazione codice ---
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

    private int calcolaNuovoId(File file) throws DAOException {
        int newId = 1;
        if (!file.exists() || file.length() <= 0) {
            return newId;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                newId = aggiornaNuovoId(newId, line);
            }
        } catch (IOException e) {
            throw new DAOException("Errore nel calcolo ID per nuovo utente", e);
        }
        return newId;
    }

    private int aggiornaNuovoId(int newId, String line) {
        String[] dati = line.split(SEPARATOR);
        if (dati.length > 0) {
            try {
                int currentId = Integer.parseInt(dati[0]);
                if (currentId >= newId) {
                    return currentId + 1;
                }
            } catch (NumberFormatException e) {
                // ignora
            }
        }
        return newId;
    }

    private String serializzaUtente(int id, Utente utente) {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(SEPARATOR);
        sb.append(utente.leggiUsername()).append(SEPARATOR);
        sb.append(utente.ottieniPassword()).append(SEPARATOR); // o password criptata
        sb.append(utente.scopriRuolo()).append(SEPARATOR);
        sb.append(utente.leggiEmail() != null ? utente.leggiEmail() : "null").append(SEPARATOR);
        sb.append(utente.leggiIndirizzo() != null ? utente.leggiIndirizzo() : "null");
        return sb.toString();
    }

    private String estraiCampoOpzionale(String[] dati, int indice) {
        if (dati.length <= indice) {
            return null;
        }

        String valore = normalizza(dati[indice]);
        return valore.isEmpty() || "null".equalsIgnoreCase(valore) ? null : valore;
    }
}
