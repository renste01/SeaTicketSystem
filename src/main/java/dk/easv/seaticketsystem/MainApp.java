package dk.easv.seaticketsystem;

// Java Imports
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setRoot("/dk/easv/seaticketsystem/Views/LoginView.fxml", 1280, 800);
        stage.setTitle("SEA Billetsystem");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }


    //Klasse til at udskifter den aktive scene med et nyt FXML.

    public static void setRoot(String fxmlClasspath, double w, double h)
    {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(fxmlClasspath));
            Scene scene = new Scene(loader.load(), w, h);
            scene.getStylesheets().add(Objects.requireNonNull(
                    MainApp.class.getResource(
                            "/dk/easv/seaticketsystem/CSS/styles.css")).toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {launch(args);}
}
