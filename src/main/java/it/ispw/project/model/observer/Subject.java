package it.ispw.project.model.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject {

    // Lista di oggetti che osservano questo subject
    // Usiamo List<Observer> per sfruttare il POLIMORFISMO
    private List<Observer> observers;

    public Subject() {
        this.observers = new ArrayList<>();
    }

    public void attach(Observer o) {
        if (o != null) observers.add(o);
    }

    public void detach(Observer o) {
        observers.remove(o);
    }

    // Metodo protetto: solo le sottoclassi (es. Carrello) possono lanciare notifiche
    protected void notifyObservers(Object data) {
        for (Observer o : observers) {
            o.update(); // Chiamata polimorfica
        }
    }
}