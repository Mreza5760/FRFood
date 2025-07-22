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
import org.FRFood.DAO.*;
import org.FRFood.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;
import org.FRFood.util.JwtUtil;
import org.FRFood.util.Role;
import org.FRFood.util.Status;

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
    private static int menuId;
    private static String menuTitle;
    private static int restaurantId;
    public Button addFoodsButton;

    public static void setData(int menuId, String menuTitle, int restaurantId) {
        MenuController.menuId = menuId;
        MenuController.menuTitle = menuTitle;
        MenuController.restaurantId = restaurantId;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/restaurant.fxml", menu_name_label);
    }

    @FXML
    public void addFood(ActionEvent actionEvent) {
        AddFoodToMenuController.setData(menuId, menuTitle, restaurantId);
        SceneNavigator.switchTo("/frontend/addFoodToMenu.fxml", menu_name_label);
    }

    @FXML
    private void initialize() {
        String token = SessionManager.getAuthToken();
        if (token == null) return;

        Jws<Claims> claimsJws = JwtUtil.validateToken(token);
        int userId = Integer.parseInt(claimsJws.getBody().getSubject());

        UserDAO userDao = new UserDAOImp();
        try {
            User user = userDao.getById(userId).orElse(null);
            if (user == null) return;
            userRole = user.getRole();
            currentUser = user;
            if (userRole == Role.buyer) {
                addFoodsButton.setVisible(false);
                addFoodsButton.setManaged(false);
            }
            menu_name_label.setText(menuTitle);
            fetchFoods();
        } catch (
                SQLException e) {
            System.out.println(e);
        }

        ;

    }

    private void fetchFoods() {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurantId + "/items/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
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

        HBox rightBox = new HBox();

        if (userRole == Role.seller) {
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


            // Add both buttons to VBox
            rightBox = new HBox(10, removeBtn, addBtn);
            rightBox.setAlignment(Pos.CENTER_RIGHT);
        }
        card.setOnMouseClicked(e -> handleClick(food)); // full card click

        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleRemove(Food food) {
        Map<Integer, Order> cart = SessionManager.getOrderList();
        Order order = new Order();

        if(cart.containsKey(restaurantId)) {
            order = cart.get(restaurantId);
        }else{
            Order tempOrder = new Order();
            cart.put(restaurantId, tempOrder);
            order = cart.get(restaurantId);
        }

        boolean found = false;

        for (OrderItem orderItem : order.getItems()) {
            if (orderItem.getItemId().equals(food.getId())) {
                if(orderItem.getQuantity() == 0){
                    return;
                }
                orderItem.setQuantity(orderItem.getQuantity() -1);
                found = true;
            }
        }

        if (!cart.containsKey(restaurantId)) {
            order.setDeliveryAddress(currentUser.getAddress());
            order.setCustomerId(currentUser.getId());
            order.setRestaurantId(restaurantId);
            order.setCouponId(0);
            RestaurantDAO restaurantDAO = new RestaurantDAOImp();
            try {
                Restaurant restaurant = restaurantDAO.getById(restaurantId).orElse(null);
                order.setTaxFee(restaurant.getTaxFee());
                order.setAdditionalFee(restaurant.getAdditionalFee());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Random rand = new Random();
            int randomPrice = rand.nextInt(91) + 10;
            order.setCourierFee(randomPrice);
            order.setCourierId(0);
            order.setStatus(Status.unpaid);
        }


        if (!found) {
            System.out.println("error so bad");
        }

        order.setRawPrice(order.getRawPrice() - food.getPrice());

        order.setPayPrice(order.getCourierFee() + order.getRawPrice() + order.getTaxFee() + order.getAdditionalFee());

    }

    private void handleAdd(Food food) {
        Map<Integer, Order> cart = SessionManager.getOrderList();
        Order order = new Order();
        if (!cart.containsKey(restaurantId)) {
            Order tempOrder = new Order();
            cart.put(restaurantId, tempOrder);
            order = cart.get(restaurantId);
            order.setDeliveryAddress(currentUser.getAddress());
            order.setCustomerId(currentUser.getId());
            order.setRestaurantId(restaurantId);
            order.setCouponId(0);
            RestaurantDAO restaurantDAO = new RestaurantDAOImp();
            try {
                Restaurant restaurant = restaurantDAO.getById(restaurantId).orElse(null);
                order.setTaxFee(restaurant.getTaxFee());
                order.setAdditionalFee(restaurant.getAdditionalFee());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Random rand = new Random();
            int randomPrice = rand.nextInt(91) + 10;
            order.setCourierFee(randomPrice);
            order.setCourierId(0);
            order.setStatus(Status.unpaid);
        }else{
            order = cart.get(restaurantId);
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

        order.setPayPrice(order.getCourierFee() + order.getRawPrice() + order.getTaxFee() + order.getAdditionalFee());

    }

    private void handleComments(Food food) {
    }

    private void handleClick(Food food) {
        FoodDetailsController.setItemId(food.getId());
        SceneNavigator.switchTo("/frontend/FoodDetail.fxml", menu_name_label);
    }

    private void handleDelete(Food food) {
        String safeUrl = "http://localhost:8080/restaurants/" + restaurantId + "/menu/" + URLEncoder.encode(menuTitle, StandardCharsets.UTF_8) + "/" + food.getId();
        URI uri = URI.create(safeUrl);
//        String url = "http://localhost:8080/restaurants/" + restaurantId+"/menu/" + menuTitle + "/" + food.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        // Optionally refresh the list on UI thread
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
