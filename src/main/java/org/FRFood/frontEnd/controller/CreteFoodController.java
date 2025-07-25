package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.FRFood.frontEnd.Util.FoodRequest;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class CreteFoodController {
    private static int restaurantId;

    public TextField nameField;
    public TextField priceField;
    public TextField supplyField;
    public TextField descriptionField;
    public Button submitButton;
    public Button uploadLogoButton;

    @FXML public TextField keywordsTextField;
    @FXML private Label logoStatusLabel;

    private String logoBase64 = "";

    public static void setRestaurantId(int restaurantId) {
        CreteFoodController.restaurantId = restaurantId;
    }

    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/restaurant.fxml",logoStatusLabel);
    }

    @FXML
    public void initialize() {
        uploadLogoButton.setOnAction(actionEvent -> handleLogoUpload());
        submitButton.setOnAction(actionEvent -> handleSubmit());
    }

    private void handleSubmit() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String priceText = priceField.getText().trim();
        String supplyText = supplyField.getText().trim();
        String keywords = keywordsTextField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || supplyText.isEmpty() || keywords.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled out.");
            return;
        }

        List<String> keywordsList = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        int price = parseIntSafe(priceText);
        int supply = parseIntSafe(supplyText);
        if (price < 0 || supply < 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid price or supply value.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            String requestBody = mapper.writeValueAsString(new FoodRequest(
                    name,
                    logoBase64,
                    description,
                    price,
                    supply,
                    keywordsList
            ));


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/restaurants/"+restaurantId+"/item"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Food created successfully.");
                                SceneNavigator.switchTo("/frontend/restaurant.fxml", logoStatusLabel);
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to create food: " + response.body()));
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while sending the request."));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to prepare request.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}