package it.ispw.project.dao.Demo;

import it.ispw.project.dao.OrdineDAO;
import it.ispw.project.model.Ordine;

import java.util.ArrayList;
import java.util.List;

public class MemoryOrdineDAO implements OrdineDAO {

    // Tabella ordini in RAM
    private static List<Ordine> tabellaOrdini = new ArrayList<>();

    @Override
    public void insertOrdine(Ordine ordine) {
        // 1. Simulazione Auto-Increment ID
        int nuovoId = 1;
        if (!tabellaOrdini.isEmpty()) {
            // Prendo l'ID dell'ultimo ordine inserito e aggiungo 1
            nuovoId = tabellaOrdini.get(tabellaOrdini.size() - 1).leggiId() + 1;
        }

        // 2. Registro l'ID nel model usando il metodo semantico corretto
        ordine.registraIdGenerato(nuovoId);

        // 3. "Salvo" nella lista
        tabellaOrdini.add(ordine);

        System.out.println("DEMO: Ordine salvato in RAM con ID " + nuovoId);
        System.out.println("DEMO: Totale ordine: " + ordine.getTotale());
    }

    @Override
    public Ordine selectOrdineById(int id) {
        for (Ordine o : tabellaOrdini) {
            if (o.leggiId() == id) return o;
        }
        return null;
    }

    @Override
    public void updateStato(Ordine ordine) {
        // In RAM l'oggetto è condiviso, quindi tecnicamente è già aggiornato.
        // Ma per simulare la ricerca e update:
        for (Ordine o : tabellaOrdini) {
            if (o.leggiId() == ordine.leggiId()) {
                // Simuliamo update (in realtà 'o' e 'ordine' sono lo stesso oggetto)
                System.out.println("DEMO: Stato aggiornato a " + ordine.getStato());
                return;
            }
        }
    }
}