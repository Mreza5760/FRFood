package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;
import org.FRFood.frontEnd.entity.Transaction;
import org.FRFood.frontEnd.entity.TransactionMethod;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminTransactionsController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, Integer> orderIdColumn;
    @FXML private TableColumn<Transaction, Integer> userIdColumn;
    @FXML private TableColumn<Transaction, String> methodColumn;
    @FXML private TableColumn<Transaction, Integer> amountColumn;
    @FXML private TableColumn<Transaction, String> payedAtColumn;

    @FXML private TextField searchUserField;
    @FXML private ComboBox<TransactionMethod> methodFilterCombo;
    @FXML private TextField keywordSearchField;
    @FXML private Button searchButton;
    @FXML private Button backButton;

    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        payedAtColumn.setCellValueFactory(new PropertyValueFactory<>("payedAt"));

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

        payedAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter inputFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            private final DateTimeFormatter outputFormatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                } else {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(item, inputFormatter);
                        setText(dateTime.format(outputFormatter));
                    } catch (Exception e) {
                        setText(item);
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

        methodFilterCombo.getItems().clear();
        methodFilterCombo.getItems().addAll(TransactionMethod.values());
        methodFilterCombo.getItems().add(null);
        methodFilterCombo.setPromptText("All");

        loadTransactions(null, null, null);

        searchButton.setOnAction(e -> {
            String user = searchUserField.getText().trim();
            TransactionMethod method = methodFilterCombo.getValue();
            String keyword = keywordSearchField.getText().trim();
            loadTransactions(user.isEmpty() ? null : user,
                    method,
                    keyword.isEmpty() ? null : keyword);
        });

        backButton.setOnAction(e -> SceneNavigator.switchTo("/frontend/panel.fxml", backButton));
    }

    private void loadTransactions(String user, TransactionMethod method, String keyword) {
        new Thread(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder("http://localhost:8080/admin/transactions");
                boolean first = true;

                if (user != null) {
                    urlBuilder.append(first ? "?" : "&")
                            .append("user=").append(URLEncoder.encode(user, StandardCharsets.UTF_8));
                    first = false;
                }
                if (method != null) {
                    urlBuilder.append(first ? "?" : "&")
                            .append("method=").append(URLEncoder.encode(method.name(), StandardCharsets.UTF_8));
                    first = false;
                }
                if (keyword != null) {
                    urlBuilder.append(first ? "?" : "&")
                            .append("search=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
                }

                URL url = new URL(urlBuilder.toString());
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