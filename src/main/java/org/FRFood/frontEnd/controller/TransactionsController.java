package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.FRFood.entity.Transaction;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TransactionsController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, Integer> orderIdColumn;
    @FXML private TableColumn<Transaction, String> methodColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;
    @FXML private TableColumn<Transaction, Integer> amountColumn;
    @FXML private TableColumn<Transaction, String> createdAtColumn;
    @FXML private Button backButton;

    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        // Bind table columns to Transaction fields
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadTransactions();

        backButton.setOnAction(e -> {
            SceneNavigator.switchTo("/frontend/wallet.fxml",backButton);
        });
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/transactions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                InputStream responseStream = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                List<Transaction> transactions = mapper.readValue(responseStream, new TypeReference<>() {});

                Platform.runLater(() -> transactionsTable.setItems(FXCollections.observableArrayList(transactions)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}