package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
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
    private ComboBox<String> roleField;
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

    private String base64ProfileImage = null;

    private static final String REGISTER_URL = "http://localhost:8080/auth/register";

    @FXML
    public void initialize() {
        roleField.getItems().addAll("buyer", "seller", "courier");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            if (!validateFields()) return;

            String jsonRequest = buildRequestJson();

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
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                        "Successfully signed up");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(conn.getInputStream(), new TypeReference<Map<String, Object>>() {
                });

                User user = mapper.convertValue(map.get("user"), User.class);
                String token = (String) map.get("token");

                SessionManager.setCurrentUser(user);
                SessionManager.setAuthToken(token);
                SessionManager.saveSession();
                if (user.getRole() == Role.buyer) {
                    SceneNavigator.switchTo("/frontend/panel.fxml", messageLabel);
                } else {
                    SceneNavigator.switchTo("/frontend/login.fxml", messageLabel);
                }
            } else {
                handleError(conn);
            }
            conn.disconnect();
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneNavigator.switchTo("/frontend/Login.fxml", messageLabel);
    }

    @FXML
    private void chooseProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                base64ProfileImage = Base64.getEncoder().encodeToString(fileBytes);
                profileField.setText(file.getName() + " (Image Selected)");
            } catch (IOException e) {
                profileField.setText("Failed to load image");
            }
        }
    }

    private String buildRequestJson() throws JsonProcessingException {
        Map<String, Object> userData = new HashMap<>();
        userData.put("full_name", fullNameField.getText().trim());
        userData.put("password", passwordField.getText().trim());
        userData.put("email", emailField.getText().isBlank() ? null : emailField.getText().trim());
        userData.put("phone", phoneNumberField.getText().trim());
        userData.put("role", roleField.getValue());
        userData.put("address", addressField.getText().trim());
        userData.put("profileImageBase64", base64ProfileImage);

        Map<String, String> bank = new HashMap<>();
        bank.put("bank_name", bankNameField.getText().trim());
        bank.put("account_number", bankAccountField.getText().trim());

        userData.put("bank_info", bank);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(userData);
    }

    private boolean validateFields() {
        String name = fullNameField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneNumberField.getText().trim();
        String role = roleField.getValue();
        String address = addressField.getText().trim();
        String bankAccount = bankAccountField.getText().trim();
        String bankName = bankNameField.getText().trim();
        String email = emailField.getText().trim();

        resetFieldStyles();

        boolean valid = true;

        if (name.isEmpty()) {
            fullNameField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (phone.isEmpty() || !phone.matches("^\\+?\\d{10,15}$")) {
            phoneNumberField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (role == null) {
            roleField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (address.isEmpty()) {
            addressField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (bankAccount.isEmpty()) {
            bankAccountField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (bankName.isEmpty()) {
            bankNameField.setStyle("-fx-border-color: red;");
            valid = false;
        }
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            emailField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (!valid) {
            messageLabel.setText("Please fix highlighted fields.");
            return false;
        }

        return true;
    }

    private void handleError(HttpURLConnection conn) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(conn.getErrorStream());
            String error = node.has("error") ? node.get("error").asText() : "Unknown error";

            if (error.toLowerCase().contains("phone")) {
                phoneNumberField.setStyle("-fx-border-color: red;");
            }

            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Registration failed: " + error);
        } catch (Exception e) {
            messageLabel.setText("Registration failed (unknown error)");
        }
    }

    private void resetFieldStyles() {
        fullNameField.setStyle(null);
        passwordField.setStyle(null);
        phoneNumberField.setStyle(null);
        roleField.setStyle(null);
        addressField.setStyle(null);
        bankAccountField.setStyle(null);
        bankNameField.setStyle(null);
        emailField.setStyle(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}