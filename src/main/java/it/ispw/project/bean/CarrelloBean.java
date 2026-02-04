package it.ispw.project.bean;

import java.util.ArrayList;
import java.util.List;

public class CarrelloBean {

    // Lista degli articoli nel carrello (già convertiti in Bean)
    private List<ArticoloBean> listaArticoli;
    private double totale;

    public CarrelloBean() {
        this.listaArticoli = new ArrayList<>();
        this.totale = 0.0;
    }

    // --- Getters & Setters ---

    public List<ArticoloBean> getListaArticoli() {
        return listaArticoli;
    }

    public void setListaArticoli(List<ArticoloBean> listaArticoli) {
        this.listaArticoli = listaArticoli;
    }

    public double getTotale() {
        return totale;
    }

    public void setTotale(double totale) {
        this.totale = totale;
    }

    // Metodo helper per aggiungere comodamente (opzionale ma utile)
    public void aggiungiArticolo(ArticoloBean art) {
        this.listaArticoli.add(art);
    }
}
