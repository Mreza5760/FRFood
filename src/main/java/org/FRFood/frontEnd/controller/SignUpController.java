package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpController {

    @FXML
    private TextField fullNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField roleField;
    @FXML
    private TextField bankNameField;
    @FXML
    private TextField bankAccountField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField profileField;
    @FXML
    private Label messageLabel;

    private static final String REGISTER_URL = "http://localhost:8080/auth/register";

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String jsonRequest = getString();
            System.out.println(jsonRequest);
            // Send HTTP POST
            if (!chek()) return;
            URL url = new URL(REGISTER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");


            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(conn.getInputStream());
                String token = node.get("token").asText();
                SessionManager.setAuthToken(token);
                messageLabel.setStyle("-fx-text-fill: #00cc66;");
                messageLabel.setText("Registered successfully!");
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(conn.getErrorStream());
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Login failed: " + node.get("error").asText());
            }

            conn.disconnect();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Login failed: " + e);
        }
    }
    @FXML
    private void goToLogin() {
        SceneNavigator.switchTo("/frontend/Login.fxml",messageLabel);
    }

    private String getString() throws JsonProcessingException {
        Map<String, Object> userData = new HashMap<>();
        userData.put("full_name", fullNameField.getText());
        userData.put("password", passwordField.getText());
        userData.put("email", emailField.getText());
        userData.put("phone", phoneNumberField.getText());
        userData.put("role", roleField.getText());
        userData.put("address", addressField.getText());
        userData.put("profileImageBase64", profileField.getText());

        Map<String, String> bank = new HashMap<>();
        bank.put("bank_name", bankNameField.getText());
        bank.put("account_number", bankAccountField.getText());

        userData.put("bank_info", bank);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(userData);
    }

    private boolean chek() {
        String name = fullNameField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneNumberField.getText().trim();
        String role = roleField.getText().trim();
        String address = addressField.getText().trim();
        String bankAccount = bankAccountField.getText().trim();
        String bankName = bankNameField.getText().trim();

        phoneNumberField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        passwordField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        fullNameField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        roleField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        addressField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        bankAccountField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);
        bankNameField.setStyle(phone.isEmpty() ? "-fx-border-color: red;" : null);

        if (phone.isEmpty() || password.isEmpty() || name.isEmpty() || role.isEmpty() || address.isEmpty() || bankAccount.isEmpty() || bankName.isEmpty()){
            messageLabel.setText("Please fill in all fields.");
            return false;
        }
        return true;
    }

}
