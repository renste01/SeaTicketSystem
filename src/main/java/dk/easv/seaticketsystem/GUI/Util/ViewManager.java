package dk.easv.seaticketsystem.GUI.Util;

// Java Imports
import javafx.scene.layout.StackPane;

/**
 * Singleton der holder global reference til appens indholds-StackPane.
 * Bruges af controllers til navigation uden at kende MainLayoutController.
 */
public class ViewManager {

    private static ViewManager instance;
    private StackPane contentArea;

    private ViewManager() {}

    public static ViewManager getInstance()
    {
        if (instance == null) instance = new ViewManager();
        return instance;
    }

    public void setContentArea(StackPane area) {this.contentArea = area;}

    public void navigateTo(String fxmlName)
    {
        NavigationUtil.loadPage(contentArea, "/dk/easv/seaticketsystem/Views/" + fxmlName + ".fxml");
    }

    public <T> T navigateToWithController(String fxmlName)
    {
        return NavigationUtil.loadPageWithController(contentArea, "/dk/easv/seaticketsystem/Views/" + fxmlName + ".fxml");
    }
}
