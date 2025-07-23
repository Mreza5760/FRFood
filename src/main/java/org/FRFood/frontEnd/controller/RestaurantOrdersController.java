package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import org.FRFood.entity.Order;
import org.FRFood.entity.Restaurant;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static io.jsonwebtoken.lang.Strings.capitalize;

public class RestaurantOrdersController {

    @FXML
    private VBox ordersContainer;

    private final ObjectMapper mapper = new ObjectMapper();
    private static Restaurant restaurant;

    /**
     * Call this after loading FXML to set the restaurant context
     **/
    public static void setRestaurant(Restaurant inRestaurant) {
        restaurant = inRestaurant;
    }

    @FXML
    public void initialize() {
        fetchOrders();
    }

    /**
     * Fetch list of orders from backend
     **/
    private void fetchOrders() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/" + restaurant.getId() + "/orders");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());

                List<Order> orders = Arrays.asList(mapper.readValue(conn.getInputStream(), Order[].class));
                Platform.runLater(() -> displayOrders(orders));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();

        for (Order order : orders) {
            VBox card = new VBox(8);
            card.setStyle("""
                        -fx-background-color: white;
                        -fx-padding: 20;
                        -fx-background-radius: 12;
                        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0, 0, 2);
                        -fx-cursor: hand;
                    """);
            card.setMaxWidth(600);
            card.setOnMouseClicked(e -> handleOrderClick(order));

            Label idLabel = new Label("🧾 Order #" + order.getId());
            idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e2a38;");

            Label priceLabel = new Label("💰 Pay Price: " + order.getPayPrice() );
            priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label statusLabel = new Label("⏱ Status: " + capitalize(order.getStatus().name()));
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2980b9;");

            Label createdLabel = new Label("📅 Placed: " + order.getCreatedAt());
            createdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

            Label feeLabel = new Label("📦 Fees: " + (order.getTaxFee() + order.getAdditionalFee() + order.getCourierFee()) );
            feeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

            VBox.setMargin(idLabel, new Insets(0, 0, 5, 0));
            card.getChildren().addAll(idLabel, priceLabel, feeLabel, statusLabel, createdLabel);
            ordersContainer.getChildren().add(card);
        }
    }

    private void handleOrderClick(Order order) {
        PayOrderController controller = SceneNavigator.switchToWithController(
                "/frontend/payOrder.fxml",
                ordersContainer,
                PayOrderController.class
        );


        if (controller != null) {
            controller.setOrder(order,restaurant ,3);
        }
    }



    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/MyRestaurants.fxml", ordersContainer);
    }
}
