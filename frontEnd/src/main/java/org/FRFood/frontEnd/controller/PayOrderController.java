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
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        detailsBox.getChildren().clear();
        detailsBox.getChildren().addAll(
                new Label("📍 Delivery Address: " + order.getDeliveryAddress()),
                new Label("📍 Restaurant Address: " + restaurant.getAddress()),
                new Label("📦 Status: " + order.getStatus()),
                new Label("👤 Customer Name: " + getNameById(order.getCustomerId())),
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
        if (currentOrder.getStatus() == Status.completed || currentOrder.getStatus() == Status.onTheWay) {
            detailsBox.getChildren().add(new Label("⏱ Courier Name: " + getNameById(currentOrder.getCourierId())));
        }

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
            SceneNavigator.switchTo("/frontEnd/orderHistory.fxml", payCardButton);
        } else if (mode == 1) {
            SceneNavigator.switchTo("/frontEnd/cart.fxml", payCardButton);
        } else if (mode == 3) {
            SceneNavigator.switchTo("/frontEnd/restaurantOrders.fxml", payWalletButton);
        } else if (mode == 4) {
            SceneNavigator.switchTo("/frontEnd/orderHistory.fxml", payWalletButton);
        }
    }

    private void sendPaymentRequest(String method) {
        if (currentOrder.getCouponId() != 0) {
            Coupon existingCoupon = getCouponById(currentOrder.getCouponId());
            String code = existingCoupon.getCouponCode();

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
                    int currentRawPrice = currentOrder.getRawPrice();
                    if (existingCoupon.getType() == CouponType.fixed) {
                        currentRawPrice += coupon.getValue();
                    } else {
                        currentRawPrice = (int) (currentRawPrice * 100.0 / (100.0 - coupon.getValue()));
                    }

                    if (currentRawPrice < coupon.getMinPrice()) {
                        currentOrder.setRawPrice(currentRawPrice);
                        currentOrder.setCouponId(0);
                        currentOrder.calculatePayPrice();
                        Platform.runLater(() ->
                                setOrder(currentOrder, restaurant, 1));
                        showAlert("error", "you have to at least buy " + coupon.getMinPrice() + " Toman to use this", Alert.AlertType.ERROR);
                        return;
                    }
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    Coupon coupon = mapper.readValue(conn.getInputStream(), Coupon.class);
                    int currentRawPrice = currentOrder.getRawPrice();
                    if (existingCoupon.getType() == CouponType.fixed) {
                        currentOrder.setRawPrice(currentRawPrice + coupon.getValue());
                    } else {
                        currentOrder.setRawPrice((int) (currentRawPrice * 100.0 / (100.0 - coupon.getValue())));
                    }
                    currentOrder.setCouponId(0);
                    currentOrder.calculatePayPrice();
                    Platform.runLater(() ->
                            setOrder(currentOrder, restaurant, 1));
                    InputStream inputStream =conn.getErrorStream();
                    String response = new BufferedReader(new InputStreamReader(inputStream))
                            .lines().collect(Collectors.joining("\n"));
                    String errorMessage;
                    try {
                        JsonNode root = mapper.readTree(response);
                        errorMessage = root.path("error").asText("Unknown error");
                    } catch (Exception e) {
                        errorMessage = "Invalid response format";
                    }
                    showAlert("error", errorMessage, Alert.AlertType.ERROR);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            URL url = new URL("http://localhost:8080/payment/online");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            connection.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();

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
                SessionManager.saveSession();
                handleBack();
            } else {
                InputStream is = connection.getErrorStream();
                try {
                    JsonNode errnode = mapper.readTree(is);
                    String errorMessage = errnode.path("error").asText("Unknown error");

                    showAlert("Failed", errorMessage, Alert.AlertType.ERROR);
                } catch (Exception e) {
                    showAlert("Failed", connection.getResponseMessage(), Alert.AlertType.ERROR);
                }

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

                if (code >= 200 && code < 300) {
                    Platform.runLater(this::handleBack);
                } else if (code == 403) {
                    showAlert("error", "you already have an active order", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleAddRating(ActionEvent actionEvent) {
        AddRatingController.setOrderId(currentOrder.getId());
        SceneNavigator.switchTo("/frontEnd/addRating.fxml", payWalletButton);
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

    private String getNameById(int userId) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            URL url = new URL("http://localhost:8080/auth/name/" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());

            InputStream is = conn.getInputStream();
            JsonNode root = mapper.readTree(is);

            return root.get("name").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleValidateCoupon(ActionEvent actionEvent) {
        String code = CouponCodeField.getText().trim();
        if (code.isEmpty()) {
            return;
        }
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
                Order order = SessionManager.getOrderList().get(restaurant.getId());
                int currentRawPrice = order.getRawPrice();
                if (order.getCouponId() != 0) {
                    Coupon existingCoupon = getCouponById(order.getCouponId());
                    if (existingCoupon.getType() == CouponType.fixed) {
                        currentRawPrice += existingCoupon.getValue();
                    } else {
                        currentRawPrice = (int) (currentRawPrice * 100.0 / (100.0 - existingCoupon.getValue()));
                    }
                }

                if (currentRawPrice < coupon.getMinPrice()) {
                    showAlert("error", "you have to at least buy " + coupon.getMinPrice() + " Toman to use this", Alert.AlertType.ERROR);
                    return;
                }

                order.setRawPrice(currentRawPrice);

                if (coupon.getType() == CouponType.fixed) {
                    order.setRawPrice(Math.max(order.getRawPrice() - coupon.getValue(), 0));
                } else {
                    order.setRawPrice((int) (order.getRawPrice() * ((100.0 - coupon.getValue()) / 100)));
                }

                order.setCouponId(coupon.getId());
                order.calculatePayPrice();
                Platform.runLater(() ->
                        setOrder(order, restaurant, 1));
            } else {
                InputStream inputStream =conn.getErrorStream();
                String response = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));
                String errorMessage;
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response);
                    errorMessage = root.path("error").asText("Unknown error");
                } catch (Exception e) {
                    errorMessage = "Invalid response format";
                }
                showAlert("error", errorMessage, Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Coupon getCouponById(int couponId) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            URL url = new URL("http://localhost:8080/admin/coupons/" + couponId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            Coupon coupon = null;
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                JsonNode root = mapper.readTree(is);

                coupon = mapper.treeToValue(root, Coupon.class);
            } else {
                showAlert("error", "failed to load any coupons with that id" + conn.getResponseMessage(), Alert.AlertType.ERROR);
            }
            return coupon;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}