package it.ispw.project.dao.fileSystem;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;
import it.ispw.project.model.Utente;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemOrdineDAO implements OrdineDAO {

    private static final String CSV_FILE_NAME = "ordini.csv";
    private static final String SEPARATOR = ";";

    @Override
    public void insertOrdine(Ordine ordine) {
        File file = new File(CSV_FILE_NAME);
        int nuovoId = 1;

        // 1. Calcolo ID Auto-Increment
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(SEPARATOR);
                        try {
                            int idLetto = Integer.parseInt(parts[0]);
                            if (idLetto >= nuovoId) {
                                nuovoId = idLetto + 1;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Assegno il nuovo ID all'oggetto (anche se il Model non ha setter pubblico,
        // nel costruttore usato per la lettura lo useremo).
        // Nota: Nel FS simuliamo l'assegnazione salvandolo con quell'ID.

        // 2. Preparazione stringa articoli: "id:qta,id:qta"
        StringBuilder articoliStr = new StringBuilder();
        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoliAcquistati().entrySet()) {
            if (articoliStr.length() > 0) articoliStr.append(",");
            articoliStr.append(entry.getKey().leggiId())
                    .append(":")
                    .append(entry.getValue());
        }

        // 3. Scrittura su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            if (file.length() > 0) bw.newLine();

            // Formato: ID;TIMESTAMP;TOTALE;STATO;ID_CLIENTE;LISTA_ARTICOLI
            StringBuilder sb = new StringBuilder();
            sb.append(nuovoId).append(SEPARATOR);
            sb.append(ordine.getDataCreazione().getTime()).append(SEPARATOR); // Timestamp long
            sb.append(ordine.getTotale()).append(SEPARATOR);
            sb.append(ordine.getStato() == null ? "IN_ATTESA" : ordine.getStato()).append(SEPARATOR);
            sb.append(ordine.getCliente().ottieniId()).append(SEPARATOR);
            sb.append(articoliStr.toString());

            bw.write(sb.toString());

            // Aggiorniamo l'ID dell'oggetto in memoria per coerenza con la sessione
            // (Richiede che Ordine abbia un modo per settare l'ID o usare reflection,
            // ma per ora lo lasciamo gestire al ricaricamento)

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Ordine> findAll() {
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
            e.printStackTrace();
        }
        return ordini;
    }

    @Override
    public Ordine selectOrdineById(int id) {
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) {
        File file = new File(CSV_FILE_NAME);
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        // 1. Leggi tutto in memoria
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(SEPARATOR);
                        int currentId = Integer.parseInt(parts[0]);

                        if (currentId == ordine.leggiId()) {
                            // 2. Ricostruisci la riga con lo stato aggiornato
                            // Recuperiamo la stringa articoli originale o la rigeneriamo
                            // Qui rigeneriamo la riga completa basandoci sull'oggetto passato
                            String newLine = serializeOrdine(ordine);
                            lines.add(newLine);
                            updated = true;
                        } else {
                            lines.add(line);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 3. Riscrivi tutto il file solo se c'è stata modifica
        if (updated) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) { // false = sovrascrivi
                for (int i = 0; i < lines.size(); i++) {
                    bw.write(lines.get(i));
                    if (i < lines.size() - 1) {
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- HELPER METHODS ---

    /**
     * Converte una riga CSV in un oggetto Ordine.
     * Richiede l'accesso ai DAO di Utente e Articolo per ricostruire le dipendenze.
     */
    private Ordine parseLineToOrdine(String line) {
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
            System.err.println("Errore parsing riga ordine: " + line);
            e.printStackTrace();
            return null;
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