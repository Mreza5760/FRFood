package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private static final String LOGIN_URL = "http://localhost:8080/auth/login";

    @FXML
    private void handleLogin() {
        resetFieldStyles();

        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        boolean valid = true;

        if (phone.isEmpty() || !phone.matches("^\\+?\\d{10,15}$")) {
            if (!phone.equals("admin")) {
                phoneField.setStyle("-fx-border-color: red;");
                valid = false;
            }
        }
        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (!valid) {
            messageLabel.setText("Please correct the highlighted fields.");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInput = String.format("{\"phone\":\"%s\",\"password\":\"%s\"}", phone, password);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInput.getBytes());
                }

                int responseCode = conn.getResponseCode();
                ObjectMapper mapper = new ObjectMapper();

                if (responseCode == 200) {
                    JsonNode node = mapper.readTree(conn.getInputStream());
                    User currentUser = mapper.readValue(node.get("user").toString(), User.class);
                    String token = node.get("token").asText();

                    SessionManager.setAuthToken(token);
                    SessionManager.setCurrentUser(currentUser);

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Login Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Welcome, " + currentUser.getFullName() + "!");
                        alert.showAndWait();
                        SceneNavigator.switchTo("/frontend/panel.fxml", messageLabel);
                    });

                } else {
                    JsonNode node = mapper.readTree(conn.getErrorStream());
                    String error = node.has("error") ? node.get("error").asText() : "Login failed";
                    Platform.runLater(() -> {
                        phoneField.setStyle("-fx-border-color: red;");
                        passwordField.setStyle("-fx-border-color: red;");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Login failed: " + error);
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Network error. Please try again.");
                });
            }
        }).start();
    }

    private void resetFieldStyles() {
        phoneField.setStyle(null);
        passwordField.setStyle(null);
        messageLabel.setText("");
    }

    @FXML
    private void goToSignUp() {
        SceneNavigator.switchTo("/frontend/signup.fxml", messageLabel);
    }
}