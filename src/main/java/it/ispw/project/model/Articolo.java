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
    private int scorta; // Corrisponde a "quantitaDisponibile" o "scorta" nel diagramma

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
     * Corrisponde a: +Disponibilita(qtaRichiesta): boolean
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
            // Qui potremmo lanciare un'eccezione custom, per ora gestiamo base
            throw new IllegalArgumentException("La scorta non può essere negativa");
        }
        this.scorta = nuovaScorta;
    }

    // --- METODI ASTRATTI PER IL POLIMORFISMO ---

    /**
     * Metodo astratto che forza le sottoclassi a definire i propri dettagli specifici.
     * Questo abilita il BINDING DINAMICO: a runtime verrà eseguito il metodo
     * della classe concreta (es. Mangime o Utensile).
     */
    public abstract String getDettagliSpecifici();

    // --- GETTERS & SETTERS ---

    public int leggiId() { return id; }
    public String leggiDescrizione() { return descrizione; }
    public double ottieniPrezzo() { return prezzo; }
    public int ottieniScorta() { return scorta; }
}
