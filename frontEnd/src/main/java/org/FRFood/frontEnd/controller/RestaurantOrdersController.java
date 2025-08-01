package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import org.FRFood.frontEnd.entity.Order;
import org.FRFood.frontEnd.entity.Restaurant;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class RestaurantOrdersController {
    @FXML
    public ComboBox<String> statusComboBox;
    @FXML
    public TextField userIdField;
    @FXML
    public TextField courierIdField;
    @FXML
    public TextField searchField;
    @FXML
    private VBox ordersContainer;

    private final ObjectMapper mapper = new ObjectMapper();
    private static Restaurant restaurant;

    public static void setRestaurant(Restaurant inRestaurant) {
        restaurant = inRestaurant;
    }

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("All", "waiting", "preparing", "cancelled", "findingCourier", "onTheWay", "completed");
        statusComboBox.getSelectionModel().selectFirst();

        fetchOrders();
    }

    private void fetchOrders() {
        new Thread(() -> {
            try {
                String statusValue = statusComboBox.getValue();
                if ("All".equals(statusValue)) {
                    statusValue = "";
                }

                String uri = "http://localhost:8080/restaurants/" +
                        restaurant.getId() + "/orders?" +
                        "status=" + statusValue +
                        "&search=" + searchField.getText().trim() +
                        "&user=" + userIdField.getText().trim() +
                        "&courier=" + courierIdField.getText().trim();

                URL url = new URL(uri);
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

            Label priceLabel = new Label("💰 Pay Price: " + (int) order.getPayPrice());
            priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label feeLabel = new Label("📦 Fees: " + (int) (order.getTaxFee() + order.getAdditionalFee() + order.getCourierFee()));
            feeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

            Label statusLabel = new Label("⏱ Status: " + order.getStatus().name());
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2980b9;");

            Label createdLabel = new Label("📅 Placed: " + order.getCreatedAt());
            createdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

            VBox.setMargin(idLabel, new Insets(0, 0, 5, 0));
            card.getChildren().addAll(idLabel, priceLabel, feeLabel, statusLabel, createdLabel);
            ordersContainer.getChildren().add(card);
        }
    }

    private void handleOrderClick(Order order) {
        PayOrderController controller = SceneNavigator.switchToWithController(
                "/frontEnd/payOrder.fxml",
                ordersContainer,
                PayOrderController.class
        );

        if (controller != null) {
            controller.setOrder(order, restaurant, 3);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.switchTo("/frontEnd/MyRestaurants.fxml", ordersContainer);
    }

    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        fetchOrders();
    }
}