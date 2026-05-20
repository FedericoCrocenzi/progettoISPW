package it.ispw.project.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe Singleton del Model che rappresenta l'inventario del negozio.
 *
 * Il Magazzino non e' una cache tecnica della persistenza: quella responsabilita'
 * appartiene ai DAO. Questa classe conserva gli articoli caricati nel dominio e
 * centralizza le regole sulle scorte, come disponibilita', scarico merce e
 * ripristino dello stock dopo un fallimento della persistenza.
 */
public class Magazzino {

    private final Map<Integer, Articolo> stock;

    private Magazzino() {
        this.stock = new HashMap<>();
    }

    private static class MagazzinoHelper {
        private static final Magazzino INSTANCE = new Magazzino();
    }

    public static Magazzino getInstance() {
        return MagazzinoHelper.INSTANCE;
    }

    public synchronized void aggiungiArticolo(Articolo articolo) {
        if (articolo != null) {
            this.stock.put(articolo.leggiId(), articolo);
        }
    }

    /**
     * Restituisce l'entity di dominio gestita dal Magazzino.
     * Metodo mantenuto per compatibilita' con i flussi esistenti: chi lo usa
     * deve trattare l'oggetto come mutabile e modificarlo solo tramite metodi
     * di dominio del Model.
     */
    public synchronized Articolo trovaArticolo(int id) {
        return this.stock.get(id);
    }

    public synchronized boolean isVuoto() {
        return this.stock.isEmpty();
    }

    public synchronized List<Articolo> ottieniArticoli() {
        List<Articolo> articoli = new ArrayList<>();
        for (Articolo articolo : this.stock.values()) {
            articoli.add(copiaArticolo(articolo));
        }
        return Collections.unmodifiableList(articoli);
    }

    public synchronized boolean verificaDisponibilita(int idArticolo, int quantitaRichiesta) {
        if (quantitaRichiesta <= 0) {
            return false;
        }

        Articolo articolo = this.stock.get(idArticolo);
        return articolo != null && articolo.checkDisponibilita(quantitaRichiesta);
    }

    public synchronized boolean verificaCoperturaOrdine(Ordine ordine) {
        if (ordine == null || ordine.getArticoli() == null) {
            return false;
        }

        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoli().entrySet()) {
            Articolo articolo = entry.getKey();
            Integer quantita = entry.getValue();

            if (articolo == null || quantita == null ||
                    !verificaDisponibilita(articolo.leggiId(), quantita)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void scaricaMerce(int idArticolo, int quantitaDaScaricare) {
        if (quantitaDaScaricare <= 0) {
            throw new IllegalArgumentException("La quantita' da scaricare deve essere positiva.");
        }

        Articolo articolo = this.stock.get(idArticolo);
        if (articolo != null) {
            articolo.aggiornaScorta(-quantitaDaScaricare);
        }
    }

    public synchronized void ripristinaMerce(int idArticolo, int quantitaDaRipristinare) {
        if (quantitaDaRipristinare <= 0) {
            throw new IllegalArgumentException("La quantita' da ripristinare deve essere positiva.");
        }

        Articolo articolo = this.stock.get(idArticolo);
        if (articolo != null) {
            articolo.aggiornaScorta(quantitaDaRipristinare);
        }
    }

    public synchronized void scaricaMerceOrdine(Ordine ordine) {
        if (!verificaCoperturaOrdine(ordine)) {
            throw new IllegalArgumentException("Scorte insufficienti per coprire l'ordine.");
        }

        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoli().entrySet()) {
            scaricaMerce(entry.getKey().leggiId(), entry.getValue());
        }
    }

    public synchronized void ripristinaMerceOrdine(Ordine ordine) {
        if (ordine == null || ordine.getArticoli() == null) {
            throw new IllegalArgumentException("Ordine non valido per il ripristino delle scorte.");
        }

        for (Map.Entry<Articolo, Integer> entry : ordine.getArticoli().entrySet()) {
            Articolo articolo = entry.getKey();
            Integer quantita = entry.getValue();

            if (articolo != null && quantita != null) {
                ripristinaMerce(articolo.leggiId(), quantita);
            }
        }
    }

    /**
     * Mantiene la compatibilita' con il codice esistente, ma non espone piu'
     * la mappa interna del Magazzino. Il risultato e' una fotografia non
     * modificabile del catalogo corrente.
     */
    public synchronized Map<Integer, Articolo> getCatalogo() {
        Map<Integer, Articolo> catalogo = new LinkedHashMap<>();
        for (Map.Entry<Integer, Articolo> entry : this.stock.entrySet()) {
            catalogo.put(entry.getKey(), copiaArticolo(entry.getValue()));
        }
        return Collections.unmodifiableMap(catalogo);
    }

    private Articolo copiaArticolo(Articolo articolo) {
        if (articolo instanceof Mangime) {
            Mangime mangime = (Mangime) articolo;
            Date scadenza = mangime.getScadenza();
            Mangime copia = new Mangime(
                    mangime.leggiId(),
                    mangime.leggiDescrizione(),
                    mangime.ottieniPrezzo(),
                    mangime.ottieniScorta(),
                    scadenza != null ? new Date(scadenza.getTime()) : null
            );
            copia.setImmaginePath(mangime.getImmaginePath());
            return copia;
        }

        if (articolo instanceof Utensile) {
            Utensile utensile = (Utensile) articolo;
            Utensile copia = new Utensile(
                    utensile.leggiId(),
                    utensile.leggiDescrizione(),
                    utensile.ottieniPrezzo(),
                    utensile.ottieniScorta(),
                    utensile.getMateriale()
            );
            copia.setImmaginePath(utensile.getImmaginePath());
            return copia;
        }

        if (articolo instanceof Fitofarmaco) {
            Fitofarmaco fitofarmaco = (Fitofarmaco) articolo;
            Fitofarmaco copia = new Fitofarmaco(
                    fitofarmaco.leggiId(),
                    fitofarmaco.leggiDescrizione(),
                    fitofarmaco.ottieniPrezzo(),
                    fitofarmaco.ottieniScorta(),
                    fitofarmaco.isRichiedePatentino()
            );
            copia.setImmaginePath(fitofarmaco.getImmaginePath());
            return copia;
        }

        return articolo;
    }
}
