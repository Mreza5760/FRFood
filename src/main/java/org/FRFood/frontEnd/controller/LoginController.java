package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        if (phone.isEmpty()) {
            phoneField.setStyle("-fx-border-color: red;");
        } else {
            phoneField.setStyle(null);
        }

        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red;");
        } else {
            passwordField.setStyle(null);
        }

        if (phone.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }


        try {
            URL url = new URL("http://localhost:8080/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format("{\"phone\":\"%s\",\"password\":\"%s\"}", phone, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(conn.getInputStream());
                String token = node.get("token").asText();
                SessionManager.setAuthToken(token);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have logged in successfully!");

                alert.showAndWait();

                goToHome();
                // Navigate to dashboard or next screen
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(conn.getErrorStream());
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Login failed: " + node.get("error").asText());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Network error.");
        }
    }
    @FXML
    private void goToSignUp() {
        try {
            System.out.println("Going to sign up...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) phoneField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) phoneField.getScene().getWindow(); // see below
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
