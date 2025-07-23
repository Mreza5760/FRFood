package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.FRFood.entity.User;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AllUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> addressColumn;
    @FXML private TableColumn<User, Integer> walletColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> confirmedColumn;
    @FXML private TableColumn<User, Void> actionColumn;
    @FXML private Button backButton;

    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        // Bind fields
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        walletColumn.setCellValueFactory(new PropertyValueFactory<>("wallet"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));

        addActionButtons();
        loadUsers();

        backButton.setOnAction(e -> SceneNavigator.switchTo("/frontend/panel.fxml", backButton));
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            private final Button declineButton = new Button("Decline");
            private final HBox container = new HBox(10, acceptButton, declineButton);

            {
                acceptButton.setStyle("-fx-background-color: #00aa88; -fx-text-fill: white; -fx-background-radius: 5;");
                declineButton.setStyle("-fx-background-color: #cc3300; -fx-text-fill: white; -fx-background-radius: 5;");

                acceptButton.setOnAction(e -> updateUserStatus("approved"));
                declineButton.setOnAction(e -> updateUserStatus("declined"));
            }

            private void updateUserStatus(String status) {
                User user = getTableView().getItems().get(getIndex());
                new Thread(() -> {
                    try {
                        URL url = new URL("http://localhost:8080/admin/users/" + user.getId() + "/status");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        String body = "{\"status\":\"" + status + "\"}";
                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(body.getBytes(StandardCharsets.UTF_8));
                        }

                        if (conn.getResponseCode() == 200) {
                            Platform.runLater(() -> usersTable.getItems().remove(user)); // Remove from list after action
                        } else {
                            System.err.println("Failed to update user status: " + conn.getResponseCode());
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Only show buttons for unconfirmed AND non-admin users
                    setGraphic(!user.isConfirmed() && !"admin".equalsIgnoreCase(String.valueOf(user.getRole()))
                            ? container : null);
                }
            }
        });
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/admin/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                try (InputStream responseStream = conn.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<User> users = mapper.readValue(responseStream, new TypeReference<>() {});

                    // Filter out admins so they don't even show up
                    users.removeIf(u -> "admin".equalsIgnoreCase(String.valueOf(u.getRole())));

                    ObservableList<User> observableList = FXCollections.observableArrayList(users);
                    Platform.runLater(() -> usersTable.setItems(observableList));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}