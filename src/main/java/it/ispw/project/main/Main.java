package it.ispw.project.main;

import it.ispw.project.config.PersistenceConfig;
import it.ispw.project.dao.DAOFactory;
import it.ispw.project.view.ViewSwitcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private static final String APP_ICON_PATH = "/image/icona_app_definitiva.png";

    @Override
    public void start(Stage stage) throws Exception {
        applicaIconaApplicazione(stage);

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "DEMO",
                List.of("DEMO", "FILESYSTEM", "JDBC")
        );

        dialog.setTitle("Scelta Persistenza");
        dialog.setHeaderText("Modalità di persistenza dati");
        dialog.setContentText("Seleziona la modalità:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            System.exit(0);
        }

        switch (result.get()) {
            case "FILESYSTEM":
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        ViewSwitcher.registraTastoFullScreen(scene, stage);

        stage.setTitle("AgriCenter Crocenzi");
        stage.setScene(scene);
        stage.show();
    }

    private void applicaIconaApplicazione(Stage stage) throws Exception {
        try (InputStream iconStream = getClass().getResourceAsStream(APP_ICON_PATH)) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
