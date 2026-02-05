package it.ispw.project.dao.FileSystem;

import it.ispw.project.dao.UtenteDAO;
import it.ispw.project.model.Utente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileSystemUtenteDAO implements UtenteDAO {

    private static final String CSV_FILE_NAME = "utenti.csv";

    @Override
    public Utente checkCredentials(String username, String password) {
        File file = new File(CSV_FILE_NAME);

        // Se il file non esiste, ritorna null (nessun utente registrato)
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] dati = line.split(";");

                // Formato atteso: id;username;password;ruolo;email;indirizzo
                // Controllo robustezza: verifichiamo ci siano abbastanza campi
                if (dati.length >= 4) {
                    String userFile = dati[1];
                    String passFile = dati[2];

                    if (userFile.equals(username) && passFile.equals(password)) {
                        // Trovato! Costruiamo l'oggetto usando il costruttore completo
                        int id = Integer.parseInt(dati[0]);
                        String ruolo = dati[3];

                        // Gestione dei campi opzionali (potrebbero essere stringa "null" o vuoti)
                        String email = (dati.length > 4 && !dati[4].equals("null")) ? dati[4] : null;
                        String indirizzo = (dati.length > 5 && !dati[5].equals("null")) ? dati[5] : null;

                        return new Utente(id, userFile, passFile, ruolo, email, indirizzo);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }
}