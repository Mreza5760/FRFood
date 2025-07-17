package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import javafx.application.Platform;
import org.FRFood.entity.Restaurant;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.HttpURLConnection;
import java.net.URL;

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

    private String logoBase64 = "";

    @FXML
    public void initialize() {
        uploadLogoButton.setOnAction(e -> handleLogoUpload());
        submitButton.setOnAction(e -> handleSubmit());
    }

    private void handleLogoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Logo Image");
        File file = fileChooser.showOpenDialog(uploadLogoButton.getScene().getWindow());
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                logoBase64 = Base64.getEncoder().encodeToString(bytes);
                logoStatusLabel.setText("✔ Image selected");
            } catch (IOException ex) {
                logoStatusLabel.setText("✘ Error reading file");
            }
        }
    }

    private void handleSubmit() {
        String name = nameField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        double taxFee = parseDoubleSafe(taxFeeField.getText());
        double additionalFee = parseDoubleSafe(additionalFeeField.getText());

        // Create a request object
        Restaurant request = new Restaurant();
        request.setName(name);
        request.setAddress(address);
        request.setPhone(phone);
        request.setLogo(logoBase64);
        request.setTaxFee((int)taxFee);
        request.setAdditionalFee((int)additionalFee);

        new Thread(() -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(request);
                URL url = new URL("http://localhost:8080/restaurants");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (responseCode == 200 || responseCode == 201) {
                        responseLabel.setStyle("-fx-text-fill: #00aa88;");
                        responseLabel.setText("✅ Restaurant created successfully!");
                    } else {
                        responseLabel.setText("❌ Failed to create restaurant (Code " + responseCode + ")");
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> responseLabel.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }


    private double parseDoubleSafe(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
