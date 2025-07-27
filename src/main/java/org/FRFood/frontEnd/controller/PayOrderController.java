package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.FRFood.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class PayOrderController {

    public Button acceptButton;
    public Button declineButton;
    public Button addRatingButton;
    @FXML
    public TextField CouponCodeField;
    @FXML
    public Button validateTokeButton;
    @FXML
    private VBox detailsBox;
    @FXML
    private VBox itemsBox;
    @FXML
    private Button payCardButton;
    @FXML
    private Button payWalletButton;
    @FXML
    private Button foodIsReadyButton;

    @FXML
    private Button finishButton;

    private Order currentOrder;
    private Restaurant restaurant;
    private int mode;

    public void setOrder(Order order, Restaurant restaurant, int inMode) {
        this.currentOrder = order;
        this.restaurant = restaurant;
        this.mode = inMode;


        if (mode == 1) {
            payCardButton.setVisible(true);
            payCardButton.setManaged(true);
            payWalletButton.setVisible(true);
            payWalletButton.setManaged(true);
            CouponCodeField.setVisible(true);
            CouponCodeField.setManaged(true);
            validateTokeButton.setVisible(true);
            validateTokeButton.setManaged(true);
        } else if (mode == 2 && currentOrder.getStatus() == Status.completed) {
            isOwnedByCurrentUser(currentOrder.getId()).thenAccept(isOwner -> {
                if (!isOwner) {
                    Platform.runLater(() -> {
                        addRatingButton.setVisible(true);
                        addRatingButton.setManaged(true);
                    });
                }
            });
        } else if (mode == 3 && currentOrder.getStatus() == Status.waiting) {
            acceptButton.setVisible(true);
            acceptButton.setManaged(true);
            declineButton.setVisible(true);
            declineButton.setManaged(true);
        } else if (mode == 3 && currentOrder.getStatus() == Status.preparing) {
            foodIsReadyButton.setVisible(true);
            foodIsReadyButton.setManaged(true);
        } else if (mode == 4 && currentOrder.getStatus() == Status.findingCourier) {
            acceptButton.setVisible(true);
            acceptButton.setManaged(true);
        } else if (mode == 4 && currentOrder.getStatus() == Status.onTheWay) {
            finishButton.setVisible(true);
            finishButton.setManaged(true);
        }
        // Populate details
        detailsBox.getChildren().clear();
        detailsBox.getChildren().addAll(
                new Label("📍 Delivery Address: " + order.getDeliveryAddress()),
                new Label("📍 Restaurant Address: " + restaurant.getAddress()),
                new Label("📦 Status: " + order.getStatus()),
                new Label("👤 Customer ID: " + order.getCustomerId()),
                new Label("🍽 Restaurant : " + restaurant.getName()),
                new Label("🎟 Coupon ID: " + (order.getCouponId() != null ? order.getCouponId() : "None")),
                new Label("💰 Raw Price: " + order.getRawPrice()),
                new Label("💸 Tax Fee: " + order.getTaxFee()),
                new Label("➕ Additional Fee: " + order.getAdditionalFee()),
                new Label("🚚 Courier Fee: " + order.getCourierFee()),
                new Label("💳 Total to Pay: " + order.getPayPrice())
        );

        if (mode != 1) {
            detailsBox.getChildren().add(new Label("⏱ Created at: " + order.getCreatedAt()));
        }

        // Populate order items
        itemsBox.getChildren().clear();
        for (OrderItem item : order.getItems()) {
            Food food = fetchItemDetails(item.getItemId());
            String itemText = "🍔food Name:" + food.getName() + " | " + "quantity :" +
                    item.getQuantity() + " | each:" + food.getPrice();
            Label itemLabel = new Label(itemText);
            itemLabel.setStyle("-fx-font-size: 14px;");
            itemsBox.getChildren().add(itemLabel);
        }
    }

    @FXML
    private void handlePayWithCard() {
        System.out.println("Paying with card for order ID: " + currentOrder.getId());
        sendPaymentRequest("online");
    }

    @FXML
    private void handlePayWithWallet() {
        System.out.println("Paying with wallet for order ID: " + currentOrder.getId());
        sendPaymentRequest("wallet");
    }

    public void handleBack() {
        if (mode == 2) {
            SceneNavigator.switchTo("/frontend/orderHistory.fxml", payCardButton);
        } else if (mode == 1) {
            SceneNavigator.switchTo("/frontend/cart.fxml", payCardButton);
        } else if (mode == 3) {
            SceneNavigator.switchTo("/frontend/restaurantOrders.fxml", payWalletButton);
        } else if (mode == 4) {
            SceneNavigator.switchTo("/frontend/orderHistory.fxml", payWalletButton);
        }
    }

    private void sendPaymentRequest(String method) {
        try {
            URL url = new URL("http://localhost:8080/payment/online");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            connection.setDoOutput(true);

            // Prepare the payload
            ObjectMapper mapper = new ObjectMapper();

            // Create a wrapper object
            var rootNode = mapper.createObjectNode();
            rootNode.set("order", mapper.valueToTree(currentOrder));
            rootNode.put("method", method);

            String jsonBody = mapper.writeValueAsString(rootNode);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                showAlert("Success", "Payment successful with " + method + "!", Alert.AlertType.INFORMATION);
                SessionManager.getOrderList().remove(currentOrder.getRestaurantId());
                handleBack();
            } else {
                showAlert("Failed", "Payment failed! Status: " + responseCode + connection.getResponseMessage(), Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void handleDecline(ActionEvent actionEvent) {
        updateOrderStatus(currentOrder.getId(), "cancelled");
    }

    public void handleAccept(ActionEvent actionEvent) {
        if (mode == 4) {
            updateOrderStatus(currentOrder.getId(), "onTheWay");
        } else {
            updateOrderStatus(currentOrder.getId(), "preparing");
        }
    }

    public void handleFoodReady(ActionEvent actionEvent) {
        updateOrderStatus(currentOrder.getId(), "findingCourier");
    }

    public void handleFinish(ActionEvent actionEvent) {
        updateOrderStatus(currentOrder.getId(), "completed");
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        new Thread(() -> {
            try {
                String temp = "http://localhost:8080/restaurants/orders/" + orderId;
                if (mode == 4) {
                    temp = "http://localhost:8080/deliveries/" + orderId;
                }
                URL url = new URL(temp);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
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
                    Platform.runLater(this::handleBack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void handleAddRating(ActionEvent actionEvent) {
        AddRatingController.setOrderId(currentOrder.getId());
        SceneNavigator.switchTo("/frontend/addRating.fxml", payWalletButton);
    }

    private CompletableFuture<Boolean> isOwnedByCurrentUser(int orderId) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/ratings/user/" + orderId))
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .GET()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        System.out.println("empty rate for order " + response.statusCode());
                        return true;
                    } else if (response.statusCode() == 469) {
                        return false;
                    } else {
                        System.err.println("Failed to fetch: HTTP " + response.statusCode() + " " + response.body());
                        return false;
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return false;
                });
    }

    private Food fetchItemDetails(int foodId) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            URL url = new URL("http://localhost:8080/items/" + foodId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());

            InputStream is = conn.getInputStream();
            JsonNode root = mapper.readTree(is);

            return mapper.treeToValue(root, Food.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void handlevalidateToken(ActionEvent actionEvent) {
        String code = CouponCodeField.getText().trim();

        try {
            String temp = "http://localhost:8080/coupons?" +
                    "coupon_code=" + code;
            URL url = new URL(temp);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            int resCode = conn.getResponseCode();

            if (resCode >= 200 && resCode < 300) {
                ObjectMapper mapper = new ObjectMapper();
                Coupon coupon = mapper.readValue(conn.getInputStream(), Coupon.class);
                int currentRawPrice = SessionManager.getOrderList().get(restaurant.getId()).getRawPrice();
                if (coupon.getMinPrice() >= currentRawPrice) {
                    showAlert("error", "you have to at least buy" + coupon.getMinPrice() + "to use this", Alert.AlertType.ERROR);
                }
                if (coupon.getType() == CouponType.fixed) {
                    if (currentRawPrice - coupon.getValue() < 0) {
                        SessionManager.getOrderList().get(restaurant.getId()).setRawPrice(0);
                    } else {
                        SessionManager.getOrderList().get(restaurant.getId()).setRawPrice(currentRawPrice - coupon.getValue());
                    }
                } else {
                    SessionManager.getOrderList().get(restaurant.getId()).setRawPrice(currentRawPrice * (1 - coupon.getValue() / 100));
                }
                SessionManager.getOrderList().get(restaurant.getId()).calculatePayPrice();
                Platform.runLater(() ->
                        setOrder(SessionManager.getOrderList().get(restaurant.getId()), restaurant, 1));
            } else {
                showAlert("error", conn.getResponseMessage(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
