package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.exception.DAOException; // <--- Import Fondamentale
import it.ispw.project.model.Articolo;
import it.ispw.project.model.Fitofarmaco;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import it.ispw.project.bean.ArticoloBean;
import java.io.InputStream;

public class ArticoloViewGraphicController {

    @FXML private Label lblTitolo;
    @FXML private Label lblPrezzo;
    @FXML private Label lblDescrizione;
    @FXML private Label lblPatentino;
    @FXML private Label lblQuantita;
    @FXML private ImageView imgProdotto;

    private Articolo articoloCorrente;
    private int quantitaSelezionata = 1;
    private String sessionId; // <--- Ci serve per collegarci alla sessione giusta

    // Non lo istanziamo subito con 'new ...()' perché ci serve il sessionId
    private AcquistaArticoloControllerApplicativo appController;

    /**
     * Chiamato dalla MainView.
     * AGGIORNAMENTO: Passiamo anche il sessionId per inizializzare correttamente il controller.
     */
    public void setDatiArticolo(Articolo articolo, String sessionId) {
        this.articoloCorrente = articolo;
        this.sessionId = sessionId;

        // INIZIALIZZAZIONE CORRETTA DEL CONTROLLER
        // Collega questo controller grafico alla sessione utente esistente (e al suo carrello)
        this.appController = new AcquistaArticoloControllerApplicativo(sessionId);

        // 1. Impostiamo i testi
        lblTitolo.setText(articolo.leggiDescrizione()); // Usa metodo del Model (Information Hiding)
        lblPrezzo.setText(String.format("€ %.2f", articolo.ottieniPrezzo()));
        lblDescrizione.setText(generaDescrizioneDettagliata(articolo));

        // 2. Gestione visibilità avviso Patentino
        if (articolo instanceof Fitofarmaco) {
            Fitofarmaco fito = (Fitofarmaco) articolo;
            lblPatentino.setVisible(fito.isRichiedePatentino());
            lblPatentino.setManaged(fito.isRichiedePatentino());
        } else {
            lblPatentino.setVisible(false);
            lblPatentino.setManaged(false);
        }

        // 3. Caricamento Immagine
        caricaImmagine(articolo);
    }

    private String generaDescrizioneDettagliata(Articolo a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a.leggiDescrizione()).append("\n\n");
        sb.append("Disponibilità in magazzino: ").append(a.ottieniScorta()).append(" unità.\n");
        return sb.toString();
    }

    private void caricaImmagine(Articolo a) {
        // Logica per caricare l'immagine (simulata o da risorse)
        // Nota: Assicurati che i percorsi esistano o gestisci il null
        try {
            String imagePath = "/images/art_" + a.leggiId() + ".png";
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is == null) {
                is = getClass().getResourceAsStream("/images/placeholder_product.png");
            }
            if (is != null) {
                imgProdotto.setImage(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento immagine: " + e.getMessage());
        }
    }

    @FXML
    public void aumentaQuantita() {
        if (quantitaSelezionata < articoloCorrente.ottieniScorta()) {
            quantitaSelezionata++;
            aggiornaLabelQuantita();
        }
    }

    @FXML
    public void diminuisciQuantita() {
        if (quantitaSelezionata > 1) {
            quantitaSelezionata--;
            aggiornaLabelQuantita();
        }
    }

    private void aggiornaLabelQuantita() {
        lblQuantita.setText(String.valueOf(quantitaSelezionata));
    }

    @FXML
    public void aggiungiAlCarrello() {
        if (articoloCorrente != null && appController != null) {
            try {
                // Creazione Bean
                ArticoloBean articoloDaAggiungere = new ArticoloBean();
                articoloDaAggiungere.setId(articoloCorrente.leggiId());

                // Chiamata al Controller Applicativo (Ora gestiamo le eccezioni!)
                appController.aggiungiArticoloAlCarrello(articoloDaAggiungere, quantitaSelezionata);

                // Feedback positivo
                mostraMessaggio("Successo", "Articolo aggiunto al carrello!", Alert.AlertType.INFORMATION);

                // Chiudi finestra dopo aggiunta
                chiudiScheda();

            } catch (DAOException e) {
                // Errore DB: Popup Rosso
                mostraMessaggio("Errore di Sistema", "Impossibile aggiungere al carrello: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // Errore Logica (es. scorta finita nel frattempo): Popup Giallo
                mostraMessaggio("Attenzione", e.getMessage(), Alert.AlertType.WARNING);
            }
        }
    }

    @FXML
    public void acquistaSubito() {
        // Aggiunge e poi (idealmente) porta al carrello
        aggiungiAlCarrello();
        // In un'app reale qui faresti anche ViewSwitcher.switchTo(CARRELLO, ...)
        // Ma attenzione: aggiungiAlCarrello chiude la scheda.
    }

    @FXML
    public void chiudiScheda() {
        if (lblTitolo.getScene() != null && lblTitolo.getScene().getWindow() != null) {
            ((Stage) lblTitolo.getScene().getWindow()).close();
        }
    }

    // Metodo helper per i popup
    private void mostraMessaggio(String titolo, String contenuto, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}