package dk.easv.seaticketsystem.GUI.Util;

// Java Imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class NavigationUtil {

    private NavigationUtil() {}

    public static void loadPage(StackPane contentArea, String classpathFxml) {
        try {
            URL url = NavigationUtil.class.getResource(classpathFxml);
            if (url == null) { System.err.println("FXML ikke fundet: " + classpathFxml); return; }
            Node node = FXMLLoader.load(url);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T loadPageWithController(StackPane contentArea, String classpathFxml) {
        try {
            URL url = NavigationUtil.class.getResource(classpathFxml);
            if (url == null) { System.err.println("FXML ikke fundet: " + classpathFxml); return null; }
            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
