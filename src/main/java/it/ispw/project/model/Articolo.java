package it.ispw.project.model;

import java.io.Serializable;

/**
 * Classe astratta che rappresenta un generico Articolo nel magazzino.
 * Rispetta il principio dell'Information Hiding (attributi privati).
 * Implementa Serializable per la persistenza su file/stream.
 */
public abstract class Articolo implements Serializable {

    // Serial Version UID per garantire la compatibilità durante la deserializzazione
    private static final long serialVersionUID = 1L;

    // Attributi comuni definiti nel diagramma MVC (classe Articolo)
    private int id;
    private String descrizione;
    private double prezzo;
    private int scorta;

    // Costruttore
    public Articolo(int id, String descrizione, double prezzo, int scorta) {
        this.id = id;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.scorta = scorta;
    }

    // --- LOGICA DI BUSINESS COMUNE ---

    /**
     * Verifica la disponibilità in magazzino.
     * Metodo concreto ereditato da tutte le sottoclassi.
     */
    public boolean checkDisponibilita(int qtaRichiesta) {
        return this.scorta >= qtaRichiesta;
    }

    /**
     * Aggiorna la scorta dopo un acquisto o rifornimento.
     * Metodo setter con logica di validazione.
     */
    public void aggiornaScorta(int quantita) {
        int nuovaScorta = this.scorta + quantita;
        if (nuovaScorta < 0) {
            throw new IllegalArgumentException("La scorta non può essere negativa");
        }
        this.scorta = nuovaScorta;
    }

    // --- METODI ASTRATTI PER IL POLIMORFISMO ---

    /**
     * Metodo astratto che forza le sottoclassi a definire i propri dettagli specifici.
     */
    public abstract String getDettagliSpecifici();

    // --- OVERRIDE METODI OBJECT (Fondamentali per l'uso nelle MAPPE del Carrello) ---

    @Override
    public boolean equals(Object o) {
        // 1. Se è lo stesso oggetto in memoria, è uguale
        if (this == o) return true;

        // 2. Se l'altro oggetto è null o è di una classe diversa, non sono uguali
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Casting e confronto sull'ID (chiave primaria logica)
        Articolo articolo = (Articolo) o;
        return id == articolo.id;
    }

    @Override
    public int hashCode() {
        // Restituisce l'hash basato sull'ID.
        // Necessario perché oggetti uguali DEVONO avere lo stesso hash.
        return Integer.hashCode(id);
    }

    // --- METODI DI LETTURA (NO GETTER STANDARD) ---

    public int leggiId() { return id; }
    public String leggiDescrizione() { return descrizione; }
    public double ottieniPrezzo() { return prezzo; }
    public int ottieniScorta() { return scorta; }
}