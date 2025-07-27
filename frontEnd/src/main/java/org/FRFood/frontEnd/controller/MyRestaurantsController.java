package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class MyRestaurantsController {

    @FXML
    private VBox restaurantList;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        fetchRestaurants();
    }

    private void fetchRestaurants() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/restaurants/mine"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        displayRestaurants(response.body());
                    } else {
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayRestaurants(String json) {
        try {
            List<Restaurant> restaurants = mapper.readValue(json, new TypeReference<>() {
            });
            Platform.runLater(() -> {
                restaurantList.getChildren().clear();
                for (Restaurant r : restaurants) {
                    restaurantList.getChildren().add(createRestaurantCard(r));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createRestaurantCard(Restaurant r) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 6);");
        card.setPrefWidth(600);

        ImageView logo = new ImageView();
        try {
            byte[] imageData = Base64.getDecoder().decode(r.getLogo());
            logo.setImage(new Image(new ByteArrayInputStream(imageData)));
        } catch (Exception e) {
            logo.setImage(null);
        }
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + r.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");

        Label addressLabel = new Label("📍 " + r.getAddress());
        addressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label phoneLabel = new Label("📞 " + r.getPhone());
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label feeLabel = new Label("💰 Tax: " + r.getTaxFee() + " | Additional Fee: " + r.getAdditionalFee());
        feeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        info.getChildren().addAll(nameLabel, addressLabel, phoneLabel, feeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button orderBtn = new Button("Orders");
        orderBtn.setPrefWidth(100);
        orderBtn.setPrefHeight(36);
        orderBtn.setStyle("""
                    -fx-background-color: #6600cc;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        orderBtn.setOnAction(e -> handleOrder(r));

        Button updateBtn = new Button("Update");
        updateBtn.setPrefWidth(100);
        updateBtn.setPrefHeight(36);
        updateBtn.setStyle("""
                    -fx-background-color: #007acc;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                """);
        updateBtn.setOnAction(e -> handleUpdate(r));

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
        deleteBtn.setOnAction(e -> handleDelete(r));

        HBox rightBox = new HBox(10,orderBtn, updateBtn, deleteBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


        card.setOnMouseClicked(e -> handleClick(r));

        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleOrder(Restaurant r) {
        RestaurantOrdersController.setRestaurant(r);
        SceneNavigator.switchTo("/frontEnd/restaurantOrders.fxml",restaurantList);
    }

    private void handleClick(Restaurant r) {
        RestaurantController.setValues(r);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/restaurant.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = (Stage) restaurantList.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            stage.setScene(new Scene(root));
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Restaurant r) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/updateRestaurant.fxml"));
        try {
            Parent root = loader.load();

            UpdateRestaurantController controller = loader.getController();

            controller.setRestaurantData(
                    r.getId(),
                    r.getName(),
                    r.getAddress(),
                    r.getPhone(),
                    r.getTaxFee(),
                    r.getAdditionalFee(),
                    r.getLogo()
            );

            Stage stage = (Stage) restaurantList.getScene().getWindow();
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

    @FXML
    private void goBack() {
        SceneNavigator.switchTo("/frontEnd/panel.fxml", restaurantList);
    }

    private void handleDelete(Restaurant r) {
        String url = "http://localhost:8080/restaurants/" + r.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    fetchRestaurants();
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        System.out.println("Deleted restaurant: " + r.getName());
                        Platform.runLater(this::fetchRestaurants);
                    } else {
                        System.err.println("Failed to delete restaurant: HTTP " + response.statusCode());
                        String errorMessage = response.body();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Delete Failed");
                            alert.setHeaderText("Restaurant Deletion Error");
                            alert.setContentText("Failed to delete restaurant.\nError " + response.statusCode() + "\nDetails: " + errorMessage);
                            alert.showAndWait();
                        });

                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
}