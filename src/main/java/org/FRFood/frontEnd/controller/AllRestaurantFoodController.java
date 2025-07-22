package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.FRFood.entity.Food;
import org.FRFood.entity.Keyword;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class AllRestaurantFoodController {
    @FXML
    public Label restaurant_name_label;
    @FXML
    public VBox foodList;

    private final ObjectMapper mapper = new ObjectMapper();

    private static int restaurantId;
    private static String restaurantName;

    public static void setData(int input, String restaurant_name) {
        AllRestaurantFoodController.restaurantId = input;
        AllRestaurantFoodController.restaurantName = restaurant_name;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/Restaurant.fxml", foodList);
    }

    @FXML
    public void addFood(ActionEvent actionEvent) {
    }

    @FXML
    private void initialize() {
        restaurant_name_label.setText(restaurantName);
        fetchFoods();
    }

    private void fetchFoods() {
        String safeUrl = "http://localhost:8080/vendors/" + restaurantId;
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
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode() + response.body());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayFoods(String body) {
        try {
            JsonNode rootNode = mapper.readTree(body);

            // Extract node for "the thing"
            JsonNode theThingNode = rootNode.get("menu_title");
            List<Food> foods = mapper.convertValue(theThingNode, new TypeReference<List<Food>>() {
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

        // editfood Button
        Button editBtn = new Button("edit food");
        editBtn.setPrefWidth(100);
        editBtn.setPrefHeight(36);
        editBtn.setStyle("""
                    -fx-background-color: #ff9900;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        editBtn.setOnAction(e -> handleEdit(food));


        // Comments Button
        Button CommentsBtn = new Button("Comments");
        CommentsBtn.setPrefWidth(100);
        CommentsBtn.setPrefHeight(36);
        CommentsBtn.setStyle("""
                    -fx-background-color: #007acc;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        CommentsBtn.setOnAction(e -> handleComments(food));

// Delete Button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(100);
        deleteBtn.setPrefHeight(36);
        deleteBtn.setStyle("""
                    -fx-background-color: #ff4444;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        deleteBtn.setOnAction(e -> handleDelete(food));

// Add both buttons to VBox
        HBox rightBox = new HBox(10, editBtn, CommentsBtn, deleteBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


//        card.setOnMouseClicked(e -> handleClick(food)); // full card click

        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleDelete(Food food) {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurantId + "/item/" + food.getId();
        URI uri = URI.create(safeUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();
//
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        System.out.println("Deleted restaurant: " + food.getName());
                        Platform.runLater(this::fetchFoods);
                    } else {
                        System.err.println("Failed to delete restaurant: HTTP " + response.statusCode()+response.body());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    private void handleComments(Food food) {
    }

    private void handleEdit(Food food) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/updateFood.fxml"));
        try {
            Parent root = loader.load();

            UpdateFoodController controller = loader.getController();
            StringBuilder keywords = new StringBuilder();
            for(Keyword f : food.getKeywords()) {
                keywords.append(f.getName()).append(",");
            }
            keywords.deleteCharAt(keywords.length()-1);
            controller.setFoodData(restaurantId,food.getId(),food.getName(),food.getSupply(),food.getPrice(),keywords.toString(),food.getDescription(),food.getPicture());


            Stage stage = (Stage) restaurant_name_label.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            stage.setScene(new Scene(root));
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}

