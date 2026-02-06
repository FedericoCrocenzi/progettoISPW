package it.ispw.project.model.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject {

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

    // Metodo protetto
    protected void notifyObservers(Object data) {
        for (Observer o : observers) {
            // CORREZIONE QUI: Devi passare 'data' al metodo update!
            o.update(data);
        }
    }
}