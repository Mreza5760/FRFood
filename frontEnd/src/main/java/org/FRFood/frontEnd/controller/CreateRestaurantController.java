package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

public class CreateRestaurantController {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField taxFeeField;
    @FXML private TextField additionalFeeField;
    @FXML private Button uploadLogoButton;
    @FXML private Label logoStatusLabel;
    @FXML private Button submitButton;
    @FXML private Label responseLabel;
    @FXML private Button backButton;

    private String logoBase64 = "";

    @FXML
    public void initialize() {
        uploadLogoButton.setOnAction(e -> handleLogoUpload());
        submitButton.setOnAction(e -> handleSubmit());
        backButton.setOnAction(e -> goBack());
    }

    private void handleLogoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Logo Image");
        File file = fileChooser.showOpenDialog(uploadLogoButton.getScene().getWindow());
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                logoBase64 = Base64.getEncoder().encodeToString(bytes);
                Platform.runLater(() -> logoStatusLabel.setText("✔ Image selected"));
            } catch (IOException ex) {
                Platform.runLater(() -> logoStatusLabel.setText("✘ Error reading file"));
            }
        }
    }

    private void handleSubmit() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String taxFeeStr = taxFeeField.getText().trim();
        String additionalFeeStr = additionalFeeField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || taxFeeStr.isEmpty() || additionalFeeStr.isEmpty()) {
            showError("⚠ Please fill in all fields.");
            return;
        }

        if (!phone.matches("^\\+?\\d{10,15}$")) {
            showError("Invalid phone number");
            return;
        }

        int taxFee = parseIntSafe(taxFeeStr);
        int additionalFee = parseIntSafe(additionalFeeStr);
        if (taxFee < 0 || additionalFee < 0) {
            showError("⚠ Tax Fee and Additional Fee must be valid integers (≥ 0).");
            return;
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        restaurant.setLogo(logoBase64);
        restaurant.setTaxFee(taxFee);
        restaurant.setAdditionalFee(additionalFee);

        new Thread(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(restaurant);

                URL url = new URL("http://localhost:8080/restaurants");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                }

                int status = conn.getResponseCode();

                if (status == 200 || status == 201) {
                    Platform.runLater(() -> {
                        responseLabel.setStyle("-fx-text-fill: #00aa88;");
                        responseLabel.setText("✅ Restaurant created successfully!");
                        clearFields();
                    });
                } else {
                    String backendError = "Failed to create restaurant";
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) {
                        JsonNode errorNode = mapper.readTree(errorStream);
                        if (errorNode.has("error")) {
                            backendError = errorNode.get("error").asText();
                        }
                    }
                    String finalError = backendError;
                    Platform.runLater(() -> showError(finalError));
                }

            } catch (IOException e) {
                Platform.runLater(() -> showError("Error: " + e.getMessage()));
            }
        }).start();
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void showError(String message) {
        responseLabel.setStyle("-fx-text-fill: red;");
        responseLabel.setText("❌ " + message);
    }

    private void clearFields() {
        nameField.clear();
        addressField.clear();
        phoneField.clear();
        taxFeeField.clear();
        additionalFeeField.clear();
        logoStatusLabel.setText("");
        logoBase64 = "";
    }

    @FXML
    private void goBack() {
        SceneNavigator.switchTo("/frontend/panel.fxml", nameField);
    }
}