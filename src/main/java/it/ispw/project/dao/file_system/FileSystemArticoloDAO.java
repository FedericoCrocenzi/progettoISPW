package it.ispw.project.dao.file_system;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSystemArticoloDAO implements ArticoloDAO {

    private static final Logger LOGGER = Logger.getLogger(FileSystemArticoloDAO.class.getName());
    private static final String CSV_FILE_NAME = "articoli.csv";
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

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
                Articolo art = parseArticolo(line);
                if (art != null) {
                    catalogo.add(art);
                }
            }
        } catch (IOException | ParseException | NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Errore lettura file articoli.", e);
        }
        return catalogo;
    }

    @Override
    public boolean updateScorta(Articolo articoloModificato) {
        if (articoloModificato == null) {
            return false;
        }

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
            return riscriviFile(catalogo);
        }
        return false;
    }

    @Override
    public List<Articolo> selectByFilter(String descrizione, String tipo, Double min, Double max) {
        // Filtro in memoria (Java Stream o ciclo classico)
        List<Articolo> tutti = selectAllArticoli();
        List<Articolo> filtrati = new ArrayList<>();

        for (Articolo a : tutti) {
            if (rispettaFiltri(a, descrizione, tipo, min, max)) {
                filtrati.add(a);
            }
        }
        return filtrati;
    }

    // Metodo helper per salvare le modifiche
    private boolean riscriviFile(List<Articolo> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_NAME))) {
            for (Articolo a : lista) {
                bw.write(serializzaArticolo(a));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore scrittura file articoli.", e);
            return false;
        }
    }

    private Articolo parseArticolo(String line) throws ParseException {
        String[] d = line.split(";");
        // Formato: id;TIPO;descrizione;prezzo;scorta;extra;[immagine_path]

        int id = Integer.parseInt(d[0]);
        String tipo = d[1];
        String desc = d[2];
        double prezzo = Double.parseDouble(d[3]);
        int scorta = Integer.parseInt(d[4]);

        Articolo art = creaArticolo(id, tipo, desc, prezzo, scorta, d);
        applicaImmagine(art, d);
        return art;
    }

    private Articolo creaArticolo(int id, String tipo, String desc, double prezzo, int scorta, String[] dati)
            throws ParseException {
        switch (tipo) {
            case "MANGIME":
                Date scadenza = (dati.length > 5 && !dati[5].equals("null")) ? dateFmt.parse(dati[5]) : null;
                return new Mangime(id, desc, prezzo, scorta, scadenza);
            case "UTENSILE":
                String materiale = (dati.length > 5) ? dati[5] : "";
                return new Utensile(id, desc, prezzo, scorta, materiale);
            case "FITOFARMACO":
                boolean patentino = (dati.length > 5) && Boolean.parseBoolean(dati[5]);
                return new Fitofarmaco(id, desc, prezzo, scorta, patentino);
            default:
                return null;
        }
    }

    private void applicaImmagine(Articolo art, String[] dati) {
        if (art != null && dati.length > 6 && !dati[6].isBlank()) {
            art.setImmaginePath(dati[6]);
        }
    }

    private boolean rispettaFiltri(Articolo articolo, String descrizione, String tipo, Double min, Double max) {
        return descrizioneCompatibile(articolo, descrizione)
                && tipoCompatibile(articolo, tipo)
                && prezzoCompatibile(articolo, min, max);
    }

    private boolean descrizioneCompatibile(Articolo articolo, String descrizione) {
        return descrizione == null
                || descrizione.isEmpty()
                || articolo.leggiDescrizione().toLowerCase().contains(descrizione.toLowerCase());
    }

    private boolean tipoCompatibile(Articolo articolo, String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            return true;
        }
        if (tipo.equals("MANGIME")) {
            return articolo instanceof Mangime;
        }
        if (tipo.equals("UTENSILE")) {
            return articolo instanceof Utensile;
        }
        if (tipo.equals("FITOFARMACO")) {
            return articolo instanceof Fitofarmaco;
        }
        return true;
    }

    private boolean prezzoCompatibile(Articolo articolo, Double min, Double max) {
        return (min == null || articolo.ottieniPrezzo() >= min)
                && (max == null || articolo.ottieniPrezzo() <= max);
    }

    private String serializzaArticolo(Articolo articolo) {
        StringBuilder sb = new StringBuilder();
        sb.append(articolo.leggiId()).append(";");
        aggiungiDatiSpecifici(sb, articolo);
        sb.append(";");
        sb.append(articolo.getImmaginePath() != null ? articolo.getImmaginePath() : "");
        return sb.toString();
    }

    private void aggiungiDatiSpecifici(StringBuilder sb, Articolo articolo) {
        if (articolo instanceof Mangime mangime) {
            aggiungiMangime(sb, mangime);
        } else if (articolo instanceof Utensile utensile) {
            aggiungiUtensile(sb, utensile);
        } else if (articolo instanceof Fitofarmaco fitofarmaco) {
            aggiungiFitofarmaco(sb, fitofarmaco);
        }
    }

    private void aggiungiMangime(StringBuilder sb, Mangime mangime) {
        sb.append("MANGIME;");
        sb.append(mangime.leggiDescrizione()).append(";");
        sb.append(mangime.ottieniPrezzo()).append(";");
        sb.append(mangime.ottieniScorta()).append(";");
        Date scad = mangime.getScadenza();
        sb.append(scad != null ? dateFmt.format(scad) : "null");
    }

    private void aggiungiUtensile(StringBuilder sb, Utensile utensile) {
        sb.append("UTENSILE;");
        sb.append(utensile.leggiDescrizione()).append(";");
        sb.append(utensile.ottieniPrezzo()).append(";");
        sb.append(utensile.ottieniScorta()).append(";");
        sb.append(utensile.getMateriale());
    }

    private void aggiungiFitofarmaco(StringBuilder sb, Fitofarmaco fitofarmaco) {
        sb.append("FITOFARMACO;");
        sb.append(fitofarmaco.leggiDescrizione()).append(";");
        sb.append(fitofarmaco.ottieniPrezzo()).append(";");
        sb.append(fitofarmaco.ottieniScorta()).append(";");
        sb.append(fitofarmaco.isRichiedePatentino());
    }
}
