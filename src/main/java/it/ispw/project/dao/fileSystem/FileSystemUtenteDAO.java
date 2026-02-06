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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] dati = line.split(SEPARATOR);

                // Controllo robustezza riga
                if (dati.length >= 4) {
                    String userFile = dati[1];
                    String passFile = dati[2];
                    String emailFile = (dati.length > 4 && !dati[4].equals("null")) ? dati[4] : null;

                    // Verifica: (Username == Input OR Email == Input) AND Password == Input
                    boolean matchIdentifier = userFile.equals(identifier) ||
                            (emailFile != null && emailFile.equals(identifier));

                    if (matchIdentifier && passFile.equals(password)) {
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
                String[] dati = line.split(SEPARATOR);
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
        int newId = 1;

        // 1. Calcolo nuovo ID (Simulazione Auto-Increment)
        if (file.exists() && file.length() > 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] dati = line.split(SEPARATOR);
                    if (dati.length > 0) {
                        try {
                            int currentId = Integer.parseInt(dati[0]);
                            if (currentId >= newId) {
                                newId = currentId + 1;
                            }
                        } catch (NumberFormatException e) {
                            // ignora
                        }
                    }
                }
            } catch (IOException e) {
                throw new DAOException("Errore nel calcolo ID per nuovo utente", e);
            }
        }

        // 2. Scrittura in coda (append = true)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            // Se il file non è vuoto, assicuriamoci di andare a capo prima di scrivere
            if (file.length() > 0) {
                bw.newLine();
            }

            // Formato: id;username;password;ruolo;email;indirizzo
            StringBuilder sb = new StringBuilder();
            sb.append(newId).append(SEPARATOR);
            sb.append(utente.leggiUsername()).append(SEPARATOR);
            sb.append(utente.ottieniPassword()).append(SEPARATOR); // o password criptata
            sb.append(utente.scopriRuolo()).append(SEPARATOR);

            // Gestione null per email
            sb.append(utente.leggiEmail() != null ? utente.leggiEmail() : "null").append(SEPARATOR);

            // Gestione null per indirizzo
            sb.append(utente.leggiIndirizzo() != null ? utente.leggiIndirizzo() : "null");

            bw.write(sb.toString());

        } catch (IOException e) {
            throw new DAOException("Errore salvataggio utente su file", e);
        }
    }

    // --- Helper Privato per evitare duplicazione codice ---
    private Utente parseUtente(String[] dati) {
        int id = Integer.parseInt(dati[0]);
        String username = dati[1];
        String password = dati[2];
        String ruolo = dati[3];

        String email = (dati.length > 4 && !dati[4].equals("null")) ? dati[4] : null;
        String indirizzo = (dati.length > 5 && !dati[5].equals("null")) ? dati[5] : null;

        return new Utente(id, username, password, ruolo, email, indirizzo);
    }
}