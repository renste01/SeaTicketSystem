module dk.easv.seaticketsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.seaticketsystem to javafx.fxml;
    exports dk.easv.seaticketsystem;
}