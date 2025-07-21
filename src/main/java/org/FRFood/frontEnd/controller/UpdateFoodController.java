package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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

    // This will be called externally to pre-load data
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
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/" + restaurantId+"/item/" +foodId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();
                String requestBody = mapper.createObjectNode()
                        .put("name", nameField.getText())
                        .put("imageBase64", logoBase64)
                        .put("description", descriptionField.getText())
                        .put("price", Integer.parseInt(priceField.getText()))
                        .put("supply", Integer.parseInt(supplyField.getText()))
                        .put("logoBase64", logoBase64)
                        .toString();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Restaurant update response: " );
                System.out.println(conn.getResponseMessage());

                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        // Go back to restaurant list
                        SceneNavigator.switchTo("/frontend/MyRestaurants.fxml", (Node) event.getSource());
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/allRestaurantFood.fxml", descriptionField);
    }
}
