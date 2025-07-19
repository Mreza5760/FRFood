package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.FRFood.DAO.UserDAO;
import org.FRFood.DAO.UserDAOImp;
import org.FRFood.entity.Menu;
import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;
import org.FRFood.util.Authenticate;
import org.FRFood.util.JwtUtil;
import org.FRFood.util.Role;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class RestaurantController {

    @FXML
    public Label restaurant_name_label;
    private static int restaurantId;
    private static String restaurantName;
    public TextField menuTitleField;
    public HBox HboxForTitleInput;

    @FXML
    private VBox menuList;

    private final ObjectMapper mapper = new ObjectMapper();

    public static void setValues(int restaurantId, String restaurantName) {
        RestaurantController.restaurantId = restaurantId;
        RestaurantController.restaurantName = restaurantName;
    }

    @FXML
    public void initialize() {
        restaurant_name_label.setText(restaurantName);
        fetchMenus();
    }

    private void fetchMenus() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/restaurants/" + restaurantId + "/menus"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        displayMenus(response.body());
                    } else {
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayMenus(String body) {
        try {
            List<Menu> menus = mapper.readValue(body, new TypeReference<>() {
            });
            Platform.runLater(() -> {
                menuList.getChildren().clear();
                for (Menu menu : menus) {
                    menuList.getChildren().add(createMenuCard(menu));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node createMenuCard(Menu menu) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 6);");
        card.setPrefWidth(600);

        // Info
        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + menu.getTitle());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");
        info.getChildren().addAll(nameLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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
        deleteBtn.setOnAction(e -> handleDelete(menu));

// Add both buttons to VBox
        HBox rightBox = new HBox(10,deleteBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


        card.setOnMouseClicked(e -> handleClick(menu)); // full card click

        card.getChildren().addAll( info, spacer, rightBox);
        return card;
    }

    private void handleClick(Menu menu) {
    }

    private void handleDelete(Menu menu) {
    }

    private void addItemHandle(Menu menu) {
    }

    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/MyRestaurants.fxml", restaurant_name_label);
    }

    public void addMenu(ActionEvent actionEvent) {
        HboxForTitleInput.setVisible(true);
        HboxForTitleInput.setManaged(true);
    }

    public void addFood(ActionEvent actionEvent) {
    }

    public void submitMenu(ActionEvent actionEvent) {
        String jsonBody = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("title", menuTitleField.getText());
            jsonBody = mapper.writeValueAsString(jsonMap);
        }catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/restaurants/" + restaurantId + "/menu"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        fetchMenus();
                        if (response.statusCode() == 201) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Added the menu");
                            alert.setHeaderText(null);
                            alert.setContentText("You have successfully added the menu!");
                            alert.showAndWait();
                        } else {
                            System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                        }
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
}
