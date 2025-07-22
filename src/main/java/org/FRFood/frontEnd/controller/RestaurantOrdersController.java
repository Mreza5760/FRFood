package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;

public class RestaurantOrdersController {

    @FXML private VBox ordersContainer;

    private final ObjectMapper mapper = new ObjectMapper();
    private static int restaurantId;

    /** Call this after loading FXML to set the restaurant context **/
    public static void setRestaurantId(int id) {
        restaurantId = id;
    }

    @FXML
    public void initialize() {
        fetchOrders();
    }

    /** Fetch list of orders from backend **/
    private void fetchOrders() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/" + restaurantId + "/orders");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());

                JsonNode root = mapper.readTree(conn.getInputStream());
                Platform.runLater(() -> displayOrders(root));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** Render each order as a card in the VBox **/
    private void displayOrders(JsonNode ordersArray) {
        ordersContainer.getChildren().clear();

        for (JsonNode order : ordersArray) {
            VBox card = new VBox(8);
            card.setStyle("""
                -fx-background-color: white;
                -fx-padding: 16;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);
            """);
            card.setMaxWidth(800);

            Label idLabel = new Label("Order ID: " + order.get("id").asInt());
            idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e2a38;");

            Label addrLabel = new Label("📍 " + order.get("delivery_address").asText());
            Label priceLabel = new Label("💰 Pay: " + order.get("pay_price").asDouble());
            Label statusLabel = new Label("⏱ Status: " + order.get("status").asText());
            statusLabel.setStyle("-fx-text-fill: #34495e;");

            // Show item IDs
            StringBuilder items = new StringBuilder();
            Iterator<JsonNode> it = order.get("item_ids").iterator();
            while (it.hasNext()) {
                items.append(it.next().asInt());
                if (it.hasNext()) items.append(", ");
            }
            Label itemsLabel = new Label("🛒 Items: [" + items + "]");

            card.getChildren().addAll(idLabel, addrLabel, priceLabel, itemsLabel, statusLabel);

            String status = order.get("status").asText();
            if ("waiting".equalsIgnoreCase(status)) {
                HBox btnBox = new HBox(10);
                btnBox.setAlignment(Pos.CENTER_RIGHT);

                Button accept = new Button("Accept");
                accept.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6;");
                accept.setOnAction(e -> updateOrderStatus(order.get("id").asInt(), "preparing"));

                Button decline = new Button("Decline");
                decline.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 6;");
                decline.setOnAction(e -> updateOrderStatus(order.get("id").asInt(), "cancelled"));

                btnBox.getChildren().addAll(accept, decline);
                card.getChildren().add(btnBox);
            }

            ordersContainer.getChildren().add(card);
        }
    }

    /** Send PATCH to update order status, then refresh list **/
    private void updateOrderStatus(int orderId, String newStatus) {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/orders/" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String body = "{\"status\":\"" + newStatus + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes());
                }
                int code = conn.getResponseCode();
                System.out.println("PATCH status code: " + code);

                // Refresh on success
                if (code >= 200 && code < 300) {
                    Platform.runLater(this::fetchOrders);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/MyRestaurants.fxml",ordersContainer);
    }
}
