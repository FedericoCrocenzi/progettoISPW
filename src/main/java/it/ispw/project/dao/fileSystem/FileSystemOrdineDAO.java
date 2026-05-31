package it.ispw.project.dao.fileSystem;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.exception.DAOException;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSystemOrdineDAO implements OrdineDAO {

    private static final Logger LOGGER = Logger.getLogger(FileSystemOrdineDAO.class.getName());
    private static final String CSV_FILE_NAME = "ordini.csv";
    private static final String SEPARATOR = ";";
    private static final String ERRORE_LETTURA_FILE_ORDINI = "Errore lettura file ordini.";

    @Override
    public void insertOrdine(Ordine ordine) throws DAOException {
        File file = new File(CSV_FILE_NAME);
        int nuovoId = calcolaProssimoId(file);

        // Assegno il nuovo ID all'oggetto (anche se il Model non ha setter pubblico,
        // nel costruttore usato per la lettura lo useremo).
        // Nota: Nel FS simuliamo l'assegnazione salvandolo con quell'ID.

        // 2. Preparazione stringa articoli: "id:qta,id:qta"
        String articoliStr = serializzaArticoli(ordine.getArticoliAcquistati());

        // 3. Scrittura su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            if (file.length() > 0) bw.newLine();

            // Formato: ID;TIMESTAMP;TOTALE;STATO;ID_CLIENTE;LISTA_ARTICOLI
            bw.write(creaRigaOrdine(nuovoId, ordine, articoliStr));

            // Aggiorniamo l'ID dell'oggetto in memoria per coerenza con la sessione
            ordine.registraIdGenerato(nuovoId);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore scrittura file ordini.", e);
            throw new DAOException("Errore durante il salvataggio dell'ordine.", e);
        }
    }

    @Override
    public List<Ordine> findAll() throws DAOException {
        List<Ordine> ordini = new ArrayList<>();
        File file = new File(CSV_FILE_NAME);
        if (!file.exists()) return ordini;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Ordine o = parseLineToOrdine(line);
                    if (o != null) {
                        ordini.add(o);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, ERRORE_LETTURA_FILE_ORDINI, e);
            throw new DAOException("Errore durante il recupero degli ordini.", e);
        }
        return ordini;
    }

    @Override
    public List<Ordine> findByStato(String stato) throws DAOException {
        List<Ordine> ordini = new ArrayList<>();
        for (Ordine ordine : findAll()) {
            if (stato != null && stato.equals(ordine.getStato())) {
                ordini.add(ordine);
            }
        }
        return ordini;
    }

    @Override
    public Ordine selectOrdineById(int id) throws DAOException {
        File file = new File(CSV_FILE_NAME);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(SEPARATOR);
                    int currentId = Integer.parseInt(parts[0]);

                    if (currentId == id) {
                        return parseLineToOrdine(line);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Errore lettura ordine da file.", e);
            throw new DAOException("Errore durante il recupero dell'ordine.", e);
        }
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) throws DAOException {
        File file = new File(CSV_FILE_NAME);
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        if (file.exists()) {
            try {
                updated = caricaRigheAggiornate(file, ordine, lines);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, ERRORE_LETTURA_FILE_ORDINI, e);
                throw new DAOException("Errore durante l'aggiornamento dello stato ordine.", e);
            }
        }

        // 3. Riscrivi tutto il file solo se c'è stata modifica
        if (updated) {
            try {
                riscriviRigheOrdini(file, lines);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore scrittura file ordini.", e);
                throw new DAOException("Errore durante l'aggiornamento dello stato ordine.", e);
            }
        }
    }

    // --- HELPER METHODS ---

    private int calcolaProssimoId(File file) throws DAOException {
        int nuovoId = 1;
        if (!file.exists()) {
            return nuovoId;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                nuovoId = aggiornaProssimoId(nuovoId, line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, ERRORE_LETTURA_FILE_ORDINI, e);
            throw new DAOException("Errore durante il salvataggio dell'ordine.", e);
        }
        return nuovoId;
    }

    private int aggiornaProssimoId(int nuovoId, String line) {
        if (!line.trim().isEmpty()) {
            String[] parts = line.split(SEPARATOR);
            try {
                int idLetto = Integer.parseInt(parts[0]);
                if (idLetto >= nuovoId) {
                    return idLetto + 1;
                }
            } catch (NumberFormatException ignored) {}
        }
        return nuovoId;
    }

    private String serializzaArticoli(Map<Articolo, Integer> articoli) {
        StringBuilder articoliStr = new StringBuilder();
        for (Map.Entry<Articolo, Integer> entry : articoli.entrySet()) {
            if (articoliStr.length() > 0) articoliStr.append(",");
            articoliStr.append(entry.getKey().leggiId())
                    .append(":")
                    .append(entry.getValue());
        }
        return articoliStr.toString();
    }

    private String creaRigaOrdine(int nuovoId, Ordine ordine, String articoliStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(nuovoId).append(SEPARATOR);
        sb.append(ordine.getDataCreazione().getTime()).append(SEPARATOR); // Timestamp long
        sb.append(ordine.getTotale()).append(SEPARATOR);
        sb.append(ordine.getStato() == null ? "IN_ATTESA" : ordine.getStato()).append(SEPARATOR);
        sb.append(ordine.getCliente().ottieniId()).append(SEPARATOR);
        sb.append(articoliStr);
        return sb.toString();
    }

    private boolean caricaRigheAggiornate(File file, Ordine ordine, List<String> lines) throws IOException {
        boolean updated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    updated = aggiungiRigaAggiornata(lines, line, ordine) || updated;
                }
            }
        }
        return updated;
    }

    private boolean aggiungiRigaAggiornata(List<String> lines, String line, Ordine ordine) {
        String[] parts = line.split(SEPARATOR);
        int currentId = Integer.parseInt(parts[0]);

        if (currentId == ordine.leggiId()) {
            lines.add(serializeOrdine(ordine));
            return true;
        }

        lines.add(line);
        return false;
    }

    private void riscriviRigheOrdini(File file, List<String> lines) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) { // false = sovrascrivi
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) {
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Converte una riga CSV in un oggetto Ordine.
     * Richiede l'accesso ai DAO di Utente e Articolo per ricostruire le dipendenze.
     */
    private Ordine parseLineToOrdine(String line) throws DAOException {
        try {
            String[] parts = line.split(SEPARATOR);
            // Formato: ID;TIMESTAMP;TOTALE;STATO;ID_CLIENTE;LISTA_ARTICOLI

            int id = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            double totale = Double.parseDouble(parts[2]);
            String stato = parts[3];
            int idCliente = Integer.parseInt(parts[4]);
            String articoliStr = (parts.length > 5) ? parts[5] : "";

            // Ricostruzione Utente
            FileSystemUtenteDAO utenteDAO = new FileSystemUtenteDAO();
            // Nota: qui serve gestione eccezioni se aggiungi throws ai metodi DAO
            Utente cliente = null;
            try { cliente = utenteDAO.findById(idCliente); } catch (Exception e) {}

            // Ricostruzione Mappa Articoli
            Map<Articolo, Integer> mappaArticoli = new HashMap<>();
            if (!articoliStr.isEmpty()) {
                FileSystemArticoloDAO articoloDAO = new FileSystemArticoloDAO();
                String[] coppie = articoliStr.split(",");
                for (String coppia : coppie) {
                    String[] kv = coppia.split(":");
                    int idArt = Integer.parseInt(kv[0]);
                    int qta = Integer.parseInt(kv[1]);

                    Articolo art = articoloDAO.selectArticoloById(idArt);
                    if (art != null) {
                        mappaArticoli.put(art, qta);
                    }
                }
            }

            Ordine o = new Ordine(id, new Date(timestamp), cliente, mappaArticoli, totale);
            o.setStato(stato); // Assicurati che Ordine abbia setStato
            return o;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore parsing riga ordine.", e);
            throw new DAOException("Errore durante la lettura dell'ordine.", e);
        }
    }

    /**
     * Converte un oggetto Ordine in stringa CSV (usato per l'update).
     */
    private String serializeOrdine(Ordine ordine) {
        StringBuilder articoliStr = new StringBuilder();
        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoli().entrySet()) {
            if (articoliStr.length() > 0) articoliStr.append(",");
            articoliStr.append(entry.getKey().leggiId())
                    .append(":")
                    .append(entry.getValue());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ordine.leggiId()).append(SEPARATOR);
        sb.append(ordine.getDataCreazione().getTime()).append(SEPARATOR);
        sb.append(ordine.getTotale()).append(SEPARATOR);
        sb.append(ordine.getStato()).append(SEPARATOR);

        // Gestione null safety per il cliente
        int idCliente = (ordine.getCliente() != null) ? ordine.getCliente().ottieniId() : -1;
        sb.append(idCliente).append(SEPARATOR);

        sb.append(articoliStr.toString());
        return sb.toString();
    }
}
