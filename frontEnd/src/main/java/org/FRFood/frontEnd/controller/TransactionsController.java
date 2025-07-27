package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionsController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> orderIdColumn;
    @FXML private TableColumn<Transaction, String> methodColumn;
    @FXML private TableColumn<Transaction, Integer> amountColumn;
    @FXML private TableColumn<Transaction, String> payedAtColumn;
    @FXML private Button backButton;

    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        payedAtColumn.setCellValueFactory(new PropertyValueFactory<>("payedAt"));

        orderIdColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Transaction, Integer> call(TableColumn<Transaction, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            Transaction tx = getTableView().getItems().get(getIndex());
                            int amt = tx.getAmount() != null ? tx.getAmount() : 0;

                            if (item == 0) {
                                if (amt > 0) {
                                    setText("Deposit");
                                } else if (amt < 0) {
                                    setText("Withdraw");
                                } else {
                                    setText("Wallet Update");
                                }
                            } else {
                                setText(String.valueOf(item));
                            }
                        }
                    }
                };
            }
        });

        payedAtColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Transaction, String> call(TableColumn<Transaction, String> param) {
                return new TableCell<>() {
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
                };
            }
        });

        amountColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Transaction, Integer> call(TableColumn<Transaction, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(String.valueOf(Math.abs(item)));
                        }
                    }
                };
            }
        });

        loadTransactions();

        backButton.setOnAction(e -> SceneNavigator.switchTo("/frontEnd/wallet.fxml", backButton));
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/transactions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                try (InputStream responseStream = conn.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Transaction> transactions = mapper.readValue(responseStream, new TypeReference<>() {});
                    Platform.runLater(() ->
                            transactionsTable.setItems(FXCollections.observableArrayList(transactions))
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}