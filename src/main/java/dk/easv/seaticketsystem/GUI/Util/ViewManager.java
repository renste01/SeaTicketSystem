package dk.easv.seaticketsystem.GUI.Util;

import javafx.scene.layout.StackPane;

public class ViewManager {

    private static ViewManager instance;
    private StackPane contentArea;

    private ViewManager() {}

    public static ViewManager getInstance() {
        if (instance == null) instance = new ViewManager();
        return instance;
    }

    public void setContentArea(StackPane area) {
        this.contentArea = area;
    }


    public void loadView(String fxmlName) {
        NavigationUtil.loadPage(
                contentArea,
                "/dk/easv/seaticketsystem/Views/" + fxmlName
        );
    }

    public void navigateTo(String fxmlName) {
        NavigationUtil.loadPage(
                contentArea,
                "/dk/easv/seaticketsystem/Views/" + fxmlName + ".fxml"
        );
    }

    public <T> T navigateToWithController(String fxmlName) {
        return NavigationUtil.loadPageWithController(
                contentArea,
                "/dk/easv/seaticketsystem/Views/" + fxmlName + ".fxml"
        );
    }
}