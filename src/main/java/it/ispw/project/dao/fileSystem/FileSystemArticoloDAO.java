package it.ispw.project.dao.fileSystem;

import it.ispw.project.dao.ArticoloDAO;
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import it.ispw.project.model.Mangime;
import it.ispw.project.model.Utensile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSystemArticoloDAO implements ArticoloDAO {

    private static final String CSV_FILE_NAME = "articoli.csv";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Articolo selectArticoloById(int id) {
        List<Articolo> tutti = selectAllArticoli();
        for (Articolo a : tutti) {
            if (a.leggiId() == id) {
                return a;
            }
        }
        return null;
    }

    @Override
    public List<Articolo> selectAllArticoli() {
        List<Articolo> catalogo = new ArrayList<>();
        File file = new File(CSV_FILE_NAME);
        if (!file.exists()) return catalogo;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(";");
                // Formato base: id;TIPO;descrizione;prezzo;scorta;[extra]

                int id = Integer.parseInt(d[0]);
                String tipo = d[1];
                String desc = d[2];
                double prezzo = Double.parseDouble(d[3]);
                int scorta = Integer.parseInt(d[4]);

                Articolo art = null;
                switch (tipo) {
                    case "MANGIME":
                        Date scadenza = (d.length > 5 && !d[5].equals("null")) ? DATE_FMT.parse(d[5]) : null;
                        art = new Mangime(id, desc, prezzo, scorta, scadenza);
                        break;
                    case "UTENSILE":
                        String materiale = (d.length > 5) ? d[5] : "";
                        art = new Utensile(id, desc, prezzo, scorta, materiale);
                        break;
                    case "FITOFARMACO":
                        boolean patentino = (d.length > 5) && Boolean.parseBoolean(d[5]);
                        art = new Fitofarmaco(id, desc, prezzo, scorta, patentino);
                        break;
                }

                if (art != null) catalogo.add(art);
            }
        } catch (IOException | ParseException | NumberFormatException e) {
            e.printStackTrace();
        }
        return catalogo;
    }

    @Override
    public void updateScorta(Articolo articoloModificato) {
        // Logica FileSystem: Leggo tutto in RAM, aggiorno l'oggetto, riscrivo tutto il file.
        List<Articolo> catalogo = selectAllArticoli();
        boolean trovato = false;

        // 1. Aggiorno la lista in memoria
        for (int i = 0; i < catalogo.size(); i++) {
            if (catalogo.get(i).leggiId() == articoloModificato.leggiId()) {
                // Sostituisco il vecchio oggetto con quello nuovo (che ha la scorta aggiornata)
                catalogo.set(i, articoloModificato);
                trovato = true;
                break;
            }
        }

        if (trovato) {
            riscriviFile(catalogo);
        }
    }

    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        // Filtro in memoria (Java Stream o ciclo classico)
        List<Articolo> tutti = selectAllArticoli();
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tutti) {
            boolean match = true;

            // Check Descrizione
            if (descrizione != null && !descrizione.isEmpty()) {
                if (!a.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase())) match = false;
            }
            // Check Tipo (controllo di istanza)
            if (match && tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("MANGIME") && !(a instanceof Mangime)) match = false;
                else if (tipo.equals("UTENSILE") && !(a instanceof Utensile)) match = false;
                else if (tipo.equals("FITOFARMACO") && !(a instanceof Fitofarmaco)) match = false;
            }
            // Check Prezzo
            if (match && min != null && a.ottieniPrezzo() < min) match = false;
            if (match && max != null && a.ottieniPrezzo() > max) match = false;

            if (match) filtrati.add(a);
        }
        return filtrati;
    }

    // Metodo helper per salvare le modifiche
    private void riscriviFile(List<Articolo> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_NAME))) {
            for (Articolo a : lista) {
                StringBuilder sb = new StringBuilder();
                sb.append(a.leggiId()).append(";");

                // Discriminatore e campi specifici
                if (a instanceof Mangime) {
                    sb.append("MANGIME;");
                    sb.append(a.leggiDescrizione()).append(";");
                    sb.append(a.ottieniPrezzo()).append(";");
                    sb.append(a.ottieniScorta()).append(";");
                    Date scad = ((Mangime) a).getScadenza();
                    sb.append(scad != null ? DATE_FMT.format(scad) : "null");
                }
                else if (a instanceof Utensile) {
                    sb.append("UTENSILE;");
                    sb.append(a.leggiDescrizione()).append(";");
                    sb.append(a.ottieniPrezzo()).append(";");
                    sb.append(a.ottieniScorta()).append(";");
                    sb.append(((Utensile) a).getMateriale());
                }
                else if (a instanceof Fitofarmaco) {
                    sb.append("FITOFARMACO;");
                    sb.append(a.leggiDescrizione()).append(";");
                    sb.append(a.ottieniPrezzo()).append(";");
                    sb.append(a.ottieniScorta()).append(";");
                    sb.append(((Fitofarmaco) a).isRichiedePatentino());
                }

                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}