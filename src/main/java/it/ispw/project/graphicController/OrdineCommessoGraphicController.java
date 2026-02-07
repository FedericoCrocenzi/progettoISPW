package it.ispw.project.graphicController;

import it.ispw.project.applicationController.AcquistaArticoloControllerApplicativo;
import it.ispw.project.bean.ArticoloBean;
import it.ispw.project.bean.OrdineBean;
import it.ispw.project.exception.DAOException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller Grafico per la gestione del dettaglio ordine lato Commesso.
 * Corrisponde alla view: ordineCommessoView.fxml
 */
public class OrdineCommessoGraphicController {

    @FXML
    private Label lblTitoloOrdine; // Label "ORDINE N°"

    @FXML
    private VBox vboxArticoli; // Il contenitore dentro lo ScrollPane dove aggiungere le righe

    @FXML
    private Button btnChiudi; // Il bottone "X"

    @FXML
    private Button btnOrdinePronto; // Il bottone "Ordine Pronto"

    private OrdineBean ordineCorrente;
    private AcquistaArticoloControllerApplicativo appController;
    private CommessoGraphicController parentController; // Riferimento al padre per aggiornare la lista

    /**
     * Metodo chiamato per passare i dati a questa view.
     * Rispetta il pattern MVC: riceve un Bean, non un Entity.
     */
    public void initData(OrdineBean ordine, CommessoGraphicController parent) {
        this.ordineCorrente = ordine;
        this.parentController = parent;
        // Inizializza il controller applicativo (sessione recuperata o passata, qui assumo stateless o singleton session)
        this.appController = new AcquistaArticoloControllerApplicativo();

        // Imposta il titolo
        lblTitoloOrdine.setText("ORDINE N° " + ordine.getId());

        // Popola la lista degli articoli
        popolaListaArticoli(ordine.getArticoli());
    }

    /**
     * Genera dinamicamente le righe (View) basandosi sulla lista di Bean.
     */
    private void popolaListaArticoli(List<ArticoloBean> articoli) {
        // Pulisce eventuali elementi placeholder presenti nel FXML
        vboxArticoli.getChildren().clear();

        if (articoli == null || articoli.isEmpty()) {
            return;
        }

        for (ArticoloBean articolo : articoli) {
            // Creazione dinamica della riga (replica la struttura del tuo FXML)
            AnchorPane row = creaRigaArticolo(articolo);
            vboxArticoli.getChildren().add(row);
        }
    }

    /**
     * Crea il nodo grafico per un singolo articolo.
     * Replica la struttura AnchorPane definita nel tuo FXML originale.
     */
    private AnchorPane creaRigaArticolo(ArticoloBean articolo) {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefSize(318, 100);
        anchorPane.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        VBox.setMargin(anchorPane, new Insets(0, 0, 10, 0));

        // 1. Immagine
        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);
        AnchorPane.setLeftAnchor(imageView, 5.0);
        AnchorPane.setTopAnchor(imageView, 10.0); // Aggiustamento posizionamento

        // Caricamento immagine (gestione path o default)
        try {
            // Assumiamo che il nome immagine sia derivabile o nel bean. Qui uso un placeholder o logica specifica.
            // Esempio: "/main/resources/Image/" + articolo.getNome() + ".png"
            // Per sicurezza metto l'icona generica se non ho il path nel bean
            String imagePath = "/main/resources/Image/icons-logistica.png";
            imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Immagine non trovata per articolo: " + articolo.getId());
        }

        // 2. Nome Articolo
        Label lblNome = new Label(articolo.getDescrizione()); // Usa descrizione o nome dal Bean
        lblNome.setStyle("-fx-font-size: 14px;");
        AnchorPane.setLeftAnchor(lblNome, 95.0);
        AnchorPane.setTopAnchor(lblNome, 13.0);

        // 3. Prezzo
        Label lblPrezzo = new Label(String.format("€ %.2f", articolo.getPrezzo()));
        lblPrezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        AnchorPane.setLeftAnchor(lblPrezzo, 95.0);
        AnchorPane.setTopAnchor(lblPrezzo, 37.0);

        // 4. Quantità (Box Giallo)
        HBox hboxQta = new HBox();
        hboxQta.setAlignment(javafx.geometry.Pos.CENTER);
        hboxQta.setPrefSize(89, 28);
        hboxQta.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #FFF176; -fx-border-radius: 15; -fx-border-width: 2;");
        AnchorPane.setRightAnchor(hboxQta, 10.0);
        AnchorPane.setBottomAnchor(hboxQta, 10.0);

        // Nota: ArticoloBean dovrebbe avere un campo 'quantita' (quella nel carrello/ordine).
        // Se ArticoloBean rappresenta l'oggetto generico, serve un campo extra o un wrapper.
        // Qui uso getQuantita() assumendo che nel contesto dell'ordine il bean sia stato popolato con la qta ordinata.
        int qta = 1; // Default se non presente nel bean specifico
        // qta = articolo.getQuantita(); // Decommentare se presente nel bean

        Label lblQta = new Label(qta + " pz.\nrichiesto");
        lblQta.setAlignment(javafx.geometry.Pos.CENTER);
        lblQta.setStyle("-fx-font-size: 10px;");
        hboxQta.getChildren().add(lblQta);

        // 5. Label Patentino (Opzionale, logica di esempio)
        // Se nel DB c'è un flag "richiedePatentino", lo mostriamo
        // if (articolo.isRichiedePatentino()) { ... }
        Label lblPatentino = new Label("Richiede Patentino");
        lblPatentino.setStyle("-fx-text-fill: #c62828; -fx-background-color: #ffebee; -fx-background-radius: 5; -fx-padding: 5 10 5 10; -fx-font-size: 10px;");
        AnchorPane.setLeftAnchor(lblPatentino, 95.0);
        AnchorPane.setBottomAnchor(lblPatentino, 10.0);
        // Nascondi se non serve: lblPatentino.setVisible(false);

        anchorPane.getChildren().addAll(imageView, lblNome, lblPrezzo, hboxQta, lblPatentino);
        return anchorPane;
    }

    @FXML
    public void onOrdineProntoClick() {
        try {
            // Pattern MVC: Il controller grafico DELEGA al controller applicativo
            // Non fa query SQL direttamente.
            appController.confermaRitiroMerce(ordineCorrente.getId());

            mostraMessaggioSuccesso("L'ordine #" + ordineCorrente.getId() + " è pronto per il ritiro.");

            // Aggiorna la vista padre (la dashboard del commesso)
            if (parentController != null) {
                // Notifica o ricarica manuale. Dato che c'è l'Observer nel parent,
                // potrebbe aggiornarsi da solo se confermaRitiroMerce scaturisce una notifica,
                // ma per sicurezza possiamo forzare un refresh UI o chiudere semplicemente.
            }

            chiudiFinestra();

        } catch (DAOException e) {
            mostraErrore("Errore", "Impossibile aggiornare l'ordine: " + e.getMessage());
        }
    }

    @FXML
    public void onChiudiClick() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) btnChiudi.getScene().getWindow();
        stage.close();
    }

    private void mostraMessaggioSuccesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostraErrore(String titolo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}