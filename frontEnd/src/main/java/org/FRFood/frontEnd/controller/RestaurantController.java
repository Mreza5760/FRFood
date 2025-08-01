package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;


import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RestaurantController {

    @FXML
    public Label restaurant_name_label;
    private static Restaurant restaurant;
    private static String restaurantName;
    public TextField menuTitleField;
    public HBox HBoxForTitleInput;
    public Button backButton;

    private static Role userRole;
    public Button addMenuButton;
    public Button addFoodsButton;
    public Button viewFoodsButton;

    @FXML
    private VBox menuList;

    private final ObjectMapper mapper = new ObjectMapper();


    public static void setValues(Restaurant restaurant) {
        RestaurantController.restaurant = restaurant;
        RestaurantController.restaurantName = restaurant.getName();
    }

    @FXML
    public void initialize() {
        userRole = SessionManager.getCurrentUser().getRole();
        restaurant_name_label.setText(restaurantName);
        if (userRole == Role.buyer) {
            addFoodsButton.setVisible(false);
            addFoodsButton.setManaged(false);
            addMenuButton.setVisible(false);
            addMenuButton.setManaged(false);
            backButton.setOnAction((event) -> {
                SceneNavigator.switchTo("/frontEnd/allRestaurants.fxml", restaurant_name_label);
            });
        } else {
            backButton.setOnAction((event) -> {
                SceneNavigator.switchTo("/frontEnd/myRestaurants.fxml", restaurant_name_label);
            });
        }
        fetchMenus();
    }

    private void fetchMenus() {
        String url = "http://localhost:8080/restaurants/" + restaurant.getId() + "/menus";
        if (userRole == Role.buyer) {
            url = "http://localhost:8080/vendors/" + restaurant.getId();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            JsonNode root = mapper.readTree(response.body());
                            if (userRole == Role.buyer) {
                                if (!root.has("menu_titles")) {
                                    System.out.println("json is incorrect");
                                    return;
                                }
                                JsonNode menuTitles = root.get("menu_titles");
                                String titlesJson = mapper.writeValueAsString(menuTitles);
                                displayMenus(titlesJson);
                            } else {
                                displayMenus(response.body());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                        System.err.println("Response body: " + response.body());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });

    }

    private void displayMenus(String body) {
        try {
            if (userRole == Role.seller) {
                List<Menu> menus = mapper.readValue(body, new TypeReference<>() {
                });
                Platform.runLater(() -> {
                    menuList.getChildren().clear();
                    for (Menu menu : menus) {
                        menu.setRestaurant(restaurant);
                        menuList.getChildren().add(createMenuCard(menu));
                    }
                });
            } else {
                List<String> menus = mapper.readValue(body, new TypeReference<>() {
                });
                Platform.runLater(() -> {
                    menuList.getChildren().clear();
                    for (String menuTitle : menus) {
                        Menu menu = new Menu();
                        menu.setTitle(menuTitle);
                        menu.setRestaurant(restaurant);
                        menuList.getChildren().add(createMenuCard(menu));
                    }
                });
            }
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

        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + menu.getTitle());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");
        info.getChildren().addAll(nameLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox rightBox = new HBox();
        if (userRole == Role.seller) {
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
            rightBox = new HBox(10, deleteBtn);
            rightBox.setAlignment(Pos.CENTER_RIGHT);

        }
        card.setOnMouseClicked(e -> handleClick(menu));

        card.getChildren().addAll(info, spacer, rightBox);
        return card;
    }

    private void handleClick(Menu menu) {
        MenuController.setData(menu.getTitle(), restaurant, 1);
        SceneNavigator.switchTo("/frontEnd/menu.fxml", restaurant_name_label);
    }

    private void handleDelete(Menu menu) {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurant.getId() + "/menu/" + URLEncoder.encode(menu.getTitle(), StandardCharsets.UTF_8);
        URI uri = URI.create(safeUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        fetchMenus();
                        if (response.statusCode() == 200) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Added the menu");
                            alert.setHeaderText(null);
                            alert.setContentText("You have successfully removed the menu!");
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

    public void addMenu(ActionEvent actionEvent) {
        HBoxForTitleInput.setVisible(true);
        HBoxForTitleInput.setManaged(true);
    }

    public void addFood(ActionEvent actionEvent) {
        CreteFoodController.setRestaurantId(restaurant.getId());
        SceneNavigator.switchTo("/frontEnd/createFood.fxml", restaurant_name_label);
    }

    public void submitMenu(ActionEvent actionEvent) {
        String jsonBody = null;
        if (menuTitleField.getText().isEmpty()) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("title", menuTitleField.getText());
            jsonBody = mapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/restaurants/" + restaurant.getId() + "/menu"))
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

    public void viewFoods(ActionEvent actionEvent) {
        if (userRole == Role.buyer) {
            MenuController.setData("All foods",restaurant,10);
            SceneNavigator.switchTo("/frontEnd/menu.fxml", restaurant_name_label);
        } else if (userRole == Role.seller) {
            AllFoodsController.setData(restaurant.getId(), restaurantName, 3, null);
            SceneNavigator.switchTo("/frontEnd/allFoods.fxml", restaurant_name_label);
        }
    }
}