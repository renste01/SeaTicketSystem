module dk.easv.seaticketsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;


    opens dk.easv.seaticketsystem to javafx.fxml;
    exports dk.easv.seaticketsystem;
}