package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpController {

    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField roleField;
    @FXML private TextField bankNameField;
    @FXML private TextField bankAccountField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField profileField;
    @FXML private Label messageLabel;

    private static final String REGISTER_URL = "http://localhost:8080/auth/register";

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String jsonRequest = getString();
            System.out.println(jsonRequest);
            // Send HTTP POST
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
                messageLabel.setText("Registered successfully!");
            } else {
                messageLabel.setText(" Registration failed (" + responseCode + ")" + conn.getResponseMessage());
            }

            conn.disconnect();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            messageLabel.setText("❌ Error: " + e.getMessage());
        }
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
}
