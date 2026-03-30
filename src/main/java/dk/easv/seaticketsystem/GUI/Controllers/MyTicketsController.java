package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.Model.Tickets;
import dk.easv.seaticketsystem.Session.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyTicketsController {

    @FXML private TableView<Tickets> ticketTable;
    @FXML private TableColumn<Tickets, String> colEvent;
    @FXML private TableColumn<Tickets, Double> colPrice;
    @FXML private TableColumn<Tickets, String> colDate;

    private final TicketService ticketService = new TicketService();

    @FXML
    private void initialize() {

        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        var user = SessionManager.getInstance().getCurrentUser();

        ticketTable.setItems(FXCollections.observableList(
                ticketService.getTicketsForUser(user.getId())
        ));
    }
}