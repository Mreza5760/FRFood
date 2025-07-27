package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.FRFood.frontEnd.Util.FoodRequest;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateFoodController {

    @FXML private TextField nameField;
    @FXML private TextField supplyField;
    @FXML private TextField priceField;
    @FXML private TextField keywordsField;
    @FXML private TextField descriptionField;

    @FXML private ImageView foodLogo;

    private int foodId;
    private int restaurantId;
    private String logoBase64 = "";
    private final String token = SessionManager.getAuthToken();

    public void setFoodData(int id,int inputFoodId, String name, int supply, int price, String inputKeywords, String description, String logo) {
        this.restaurantId = id;
        nameField.setText(name);
        supplyField.setText(supply + "");
        priceField.setText(price + "");
        keywordsField.setText(inputKeywords);
        descriptionField.setText(description);
        this.logoBase64 = logo;
        foodId = inputFoodId;

        if (logo != null && !logo.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(logo);
            foodLogo.setImage(new Image(new ByteArrayInputStream(imageBytes)));
        }
    }

    @FXML
    public void handleChangeLogo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Logo");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageBytes = new FileInputStream(file).readAllBytes();
                logoBase64 = Base64.getEncoder().encodeToString(imageBytes);
                foodLogo.setImage(new Image(new FileInputStream(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String priceText = priceField.getText().trim();
        String supplyText = supplyField.getText().trim();
        String keywords = keywordsField.getText().trim();
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
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/" + restaurantId+"/item/" +foodId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();
                String requestBody = mapper.writeValueAsString(new FoodRequest(
                        name,
                        logoBase64,
                        description,
                        price,
                        supply,
                        keywordsList
                ));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Restaurant update response: " );
                System.out.println(conn.getResponseMessage());

                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Food updated successfully.");
                        SceneNavigator.switchTo("/frontend/allFoods.fxml", (Node) event.getSource());
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while sending the request."));
            }
        }).start();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/allFoods.fxml", descriptionField);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @FXML
    public void handleDeleteLogo(ActionEvent event) {
        foodLogo.setImage(null);
        logoBase64 = "";
        showAlert(Alert.AlertType.INFORMATION, "Deleted", "Food image removed. Click Save to confirm changes.");
    }
}