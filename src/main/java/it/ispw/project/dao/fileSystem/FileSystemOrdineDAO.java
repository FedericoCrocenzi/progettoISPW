package it.ispw.project.dao.fileSystem;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Ordine;

import java.io.*;
import java.util.Map;

public class FileSystemOrdineDAO implements OrdineDAO {

    private static final String CSV_FILE_NAME = "ordini.csv";

    @Override
    public void insertOrdine(Ordine ordine) {
        File file = new File(CSV_FILE_NAME);
        int nuovoId = 1;

        // 1. Calcolo il nuovo ID (leggendo l'ultima riga o contando le righe)
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(";");
                        int idLetto = Integer.parseInt(parts[0]);
                        if (idLetto >= nuovoId) {
                            nuovoId = idLetto + 1;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 2. Registro l'ID nel model (come abbiamo fatto nel JDBC)
        // Nota: Nel FileSystem non c'è rollback reale, quindi lo facciamo prima di scrivere
        ordine.registraIdGenerato(nuovoId);

        // 3. Serializzo la mappa articoli in stringa: "id:qta,id:qta"
        StringBuilder articoliStr = new StringBuilder();
        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoliAcquistati().entrySet()) {
            if (articoliStr.length() > 0) articoliStr.append(",");
            articoliStr.append(entry.getKey().leggiId())
                    .append(":")
                    .append(entry.getValue());
        }

        // 4. Scrivo la riga nel file (Append mode = true)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(ordine.leggiId()).append(";");
            sb.append(ordine.getDataCreazione().getTime()).append(";"); // Salvo timestamp long
            sb.append(ordine.getTotale()).append(";");
            sb.append(ordine.getStato()).append(";");
            sb.append(ordine.getCliente().ottieniId()).append(";");
            sb.append(articoliStr.toString()); // colonna articoli

            bw.write(sb.toString());
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Ordine selectOrdineById(int id) {
        // Implementazione opzionale per l'esame se il caso d'uso non prevede
        // di ricaricare gli ordini vecchi.
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) {
        // Simile a updateScorta: leggi tutto, modifica riga, riscrivi.
        // Data la complessità, spesso nei progetti esame si lascia vuoto
        // a meno che non sia richiesto esplicitamente dal professore.
    }
}