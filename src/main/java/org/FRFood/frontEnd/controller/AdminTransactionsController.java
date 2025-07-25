package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.FRFood.entity.Transaction;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AdminTransactionsController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, Integer> orderIdColumn;
    @FXML private TableColumn<Transaction, Integer> userIdColumn;
    @FXML private TableColumn<Transaction, String> methodColumn;
    @FXML private TableColumn<Transaction, Integer> amountColumn;
    @FXML private Button backButton;

    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));

        orderIdColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer orderId, boolean empty) {
                super.updateItem(orderId, empty);
                if (empty || orderId == null) {
                    setText(null);
                } else {
                    Transaction tx = getTableView().getItems().get(getIndex());
                    int amt = tx.getAmount() != null ? tx.getAmount() : 0;

                    if (orderId == 0) {
                        if (amt > 0) {
                            setText("Deposit");
                        } else if (amt < 0) {
                            setText("Withdraw");
                        } else {
                            setText("Wallet Update");
                        }
                    } else {
                        setText(orderId.toString());
                    }
                }
            }
        });

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(Math.abs(amount)));
                }
            }
        });

        loadTransactions();

        backButton.setOnAction(e -> SceneNavigator.switchTo("/frontend/panel.fxml", backButton));
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/admin/transactions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                try (InputStream responseStream = conn.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Transaction> transactions = mapper.readValue(responseStream, new TypeReference<>() {});
                    ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);

                    Platform.runLater(() -> transactionsTable.setItems(observableList));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}