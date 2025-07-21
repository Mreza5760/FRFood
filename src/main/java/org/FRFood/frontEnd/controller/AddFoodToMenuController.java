package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.FRFood.entity.Food;
import org.FRFood.entity.Menu;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFoodToMenuController {
    @FXML
    public Label menu_name_label;
    @FXML
    public VBox foodList;

    private final ObjectMapper mapper = new ObjectMapper();

    private static int menuId;
    private static String menuTitle;
    private static int restaurantId;

    public static void setData(int menu_id, String menu_Title, int restaurant_Id) {
        menuId = menu_id;
        menuTitle = menu_Title;
        restaurantId = restaurant_Id;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/menu.fxml", menu_name_label);
    }

    @FXML
    private void initialize() {
        menu_name_label.setText(menuTitle);
        fetchFoods();
    }

    private void fetchFoods() {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurantId + "/menu/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
        URI uri = URI.create(safeUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        displayFoods(response.body());
                    } else {
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayFoods(String body) {
        try {
            List<Food> foods = mapper.readValue(body, new TypeReference<>() {
            });
            Platform.runLater(() -> {
                foodList.getChildren().clear();
                for (Food food : foods) {
                    foodList.getChildren().add(createFoodCard(food));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createFoodCard(Food food) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 6);");
        card.setPrefWidth(600);

        // Logo
        ImageView logo = new ImageView();
        try {
            byte[] imageData = Base64.getDecoder().decode(food.getPicture());
            logo.setImage(new Image(new ByteArrayInputStream(imageData)));
        } catch (Exception e) {
            logo.setImage(null); // fallback if needed
        }
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        // Info
        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + food.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");

        Label supplyLabel = new Label("📍 Supply: " + food.getSupply());
        supplyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label feeLabel = new Label("💰 Price: " + food.getPrice() + "$");
        feeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label descriptionLabel = new Label("📝 " + food.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        info.getChildren().addAll(nameLabel, supplyLabel, feeLabel, descriptionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


// Delete Button
        Button addBtn = new Button("add");
        addBtn.setPrefWidth(100);
        addBtn.setPrefHeight(36);
        addBtn.setStyle("""
                    -fx-background-color: #00aa88;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        addBtn.setOnAction(e -> handleaddBtn(food));

// Add both buttons to VBox
        HBox rightBox = new HBox(10, addBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


        card.setOnMouseClicked(e -> handleClick(food)); // full card click

        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleaddBtn(Food food) {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurantId + "/menu/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
        URI uri = URI.create(safeUrl);

        Map<String, Integer> map = new HashMap<>();
        map.put("item_id", food.getId());
        String json = "";
        try {
            json = mapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        Platform.runLater(() -> {
                            fetchFoods();
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Food added successfully.");
                        });
                    } else {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to add food: " + response.body()));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while sending the request."));
                    return null;
                });

    }

    private void handleClick(Food food) {
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
