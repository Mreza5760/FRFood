package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.FRFood.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;


import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MenuController {
    @FXML
    public Label menu_name_label;
    @FXML
    public VBox foodList;

    private final ObjectMapper mapper = new ObjectMapper();

    private static Role userRole;
    private static User currentUser;
    private static String menuTitle;
    private static Restaurant restaurant;
    private static int mode;
    public Button addFoodsButton;

    public static void setData(String menuTitle, Restaurant inRestaurant, int inMode) {
        MenuController.menuTitle = menuTitle;
        MenuController.restaurant = inRestaurant;
        MenuController.mode = inMode;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        if (mode == 2) {
            SceneNavigator.switchTo("/frontend/topOffers.fxml", menu_name_label);
        }else{
        SceneNavigator.switchTo("/frontend/restaurant.fxml", menu_name_label);}
    }

    @FXML
    public void addFood(ActionEvent actionEvent) {
        AddFoodToMenuController.setData(menuTitle, restaurant.getId());
        SceneNavigator.switchTo("/frontend/addFoodToMenu.fxml", menu_name_label);
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getCurrentUser();
        userRole = currentUser.getRole();
        if (userRole == Role.buyer) {
            addFoodsButton.setVisible(false);
            addFoodsButton.setManaged(false);
        }
        menu_name_label.setText(menuTitle);
        fetchFoods();
    }

    private void fetchFoods() {
        String safeUrl = "";
        if (mode == 1) {
            safeUrl = "http://localhost:8080/restaurants/" + restaurant.getId() + "/items/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);

        }
        if (mode == 2) {
            safeUrl = "http://localhost:8080/top/foods";
        }

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

        ImageView logo = new ImageView();
        try {
            byte[] imageData = Base64.getDecoder().decode(food.getPicture());
            logo.setImage(new Image(new ByteArrayInputStream(imageData)));
        } catch (Exception e) {
            logo.setImage(null);
        }
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + food.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");

        Label supplyLabel = new Label("📍 Supply: " + food.getSupply());
        supplyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label feeLabel = new Label("💰 Price: " + food.getPrice() + " Toman");
        feeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label descriptionLabel = new Label("📝 " + food.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        info.getChildren().addAll(nameLabel, supplyLabel, feeLabel, descriptionLabel);

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
            deleteBtn.setOnAction(e -> handleDelete(food));
            rightBox = new HBox(10, deleteBtn);
            rightBox.setAlignment(Pos.CENTER_RIGHT);
        }

        if (userRole == Role.buyer) {
            // remove Button
            Button removeBtn = new Button("-");
            removeBtn.setMaxSize(36, 36);
            removeBtn.setStyle("""
                        -fx-background-color: #ff4444;
                        -fx-text-fill: white;
                        -fx-font-size: 20px;
                        -fx-font-weight: bold;
                        -fx-background-radius: 10;
                        -fx-cursor: hand;
                    """);
            removeBtn.setOnAction(e -> handleRemove(food));

            Order temp = SessionManager.getOrderList().get(restaurant.getId());
            OrderItem theFood = null;
            if (temp != null) {
                for (OrderItem order : temp.getItems()) {
                    if (order.getItemId().equals(food.getId())) {
                        theFood = order;
                    }
                }
            }

            // add Button
            Button addBtn = new Button("+");
            addBtn.setMaxSize(36, 36);
            addBtn.setStyle("""
                        -fx-background-color: #00aa88;
                        -fx-text-fill: white;
                        -fx-font-size: 20px;
                        -fx-font-weight: bold;
                        -fx-background-radius: 10;
                        -fx-cursor: hand;
                    """);
            addBtn.setOnAction(e -> handleAdd(food));

            if (theFood != null) {
                Label counter = new Label(theFood.getQuantity().toString());
                counter.setStyle("-fx-text-fill: black;");
                rightBox = new HBox(10, removeBtn, counter, addBtn);
            } else {
                rightBox = new HBox(10, removeBtn, addBtn);
            }
            // Add both buttons to VBox

            rightBox.setAlignment(Pos.CENTER_RIGHT);
        }
        card.setOnMouseClicked(e -> handleClick(food)); // full card click

        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleRemove(Food food) {
        Map<Integer, Order> cart = SessionManager.getOrderList();

        if (!cart.containsKey(restaurant.getId())) {
            return;
        }
        Order order = cart.get(restaurant.getId());

        boolean found = false;

        for (OrderItem orderItem : order.getItems()) {
            if (orderItem.getItemId().equals(food.getId())) {
                if (orderItem.getQuantity() == 0) {
                    return;
                }
                orderItem.setQuantity(orderItem.getQuantity() - 1);
                found = true;
            }
        }

        if (!found) {
            System.out.println("error so bad");
        }

        boolean empty = true;
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem.getQuantity() != 0) {
                empty = false;
                break;
            }
        }
        if (empty) {
            cart.remove(restaurant.getId());
        }

        order.setRawPrice(order.getRawPrice() - food.getPrice());

        order.calculatePayPrice();

        Platform.runLater(this::fetchFoods);
    }

    private void handleAdd(Food food) {
        Map<Integer, Order> cart = SessionManager.getOrderList();
        Order order = new Order();
        if (!cart.containsKey(restaurant.getId())) {
            Order tempOrder = new Order();
            cart.put(restaurant.getId(), tempOrder);
            order = cart.get(restaurant.getId());
            order.setDeliveryAddress(currentUser.getAddress());
            order.setCustomerId(currentUser.getId());
            order.setRestaurantId(restaurant.getId());
            order.setCouponId(0);
            order.setTaxFee(restaurant.getTaxFee());
            order.setAdditionalFee(restaurant.getAdditionalFee());
            Random rand = new Random();
            int randomPrice = rand.nextInt(91) + 10;
            order.setCourierFee(randomPrice);
            order.setCourierId(0);
            order.setStatus(Status.unpaid);
        } else {
            order = cart.get(restaurant.getId());
        }

        boolean found = false;
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem.getItemId().equals(food.getId())) {
                orderItem.setQuantity(orderItem.getQuantity() + 1);
                found = true;
            }
        }
        if (!found) {
            order.getItems().add(new OrderItem(food.getId(), 1));
        }

        order.setRawPrice(order.getRawPrice() + food.getPrice());

        order.calculatePayPrice();

        Platform.runLater(this::fetchFoods);

    }

    private void handleClick(Food food) {
        FoodDetailsController.setItemId(food.getId());
        SceneNavigator.switchTo("/frontend/FoodDetail.fxml", menu_name_label);
    }

    private void handleDelete(Food food) {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurant.getId() + "/menu/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8) + "/" + food.getId();
        URI uri = URI.create(safeUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        Platform.runLater(this::fetchFoods);
                    } else {
                        System.err.println("Failed to delete food: HTTP " + response.statusCode() + response.body());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
}