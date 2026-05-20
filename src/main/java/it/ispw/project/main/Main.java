package it.ispw.project.main;

import it.ispw.project.config.PersistenceConfig;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // 1 Popup scelta persistenza (BLOCCANTE)
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "DEMO",
                List.of("DEMO", "FILESYSTEM", "JDBC")
        );

        dialog.setTitle("Scelta Persistenza");
        dialog.setHeaderText("Modalità di persistenza dati");
        dialog.setContentText("Seleziona la modalità:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            // Utente chiude il popup → uscita pulita
            System.exit(0);
        }

        // 2 Traduzione scelta → costante DAO
        switch (result.get()) {
            case "FILE_SYSTEM":
                PersistenceConfig.setPersistenceType(DAOFactory.FILESYSTEM);
                break;
            case "JDBC":
                PersistenceConfig.setPersistenceType(DAOFactory.JDBC);
                break;
            case "DEMO":
            default:
                PersistenceConfig.setPersistenceType(DAOFactory.DEMO);
                break;
        }

        // 3 Caricamento Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        ViewSwitcher.registraTastoFullScreen(scene, stage);

        stage.setTitle("AgriCenter Crocenzi");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}