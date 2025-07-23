package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.FRFood.entity.Order;
import org.FRFood.entity.OrderItem;
import org.FRFood.entity.Restaurant;
import org.FRFood.frontEnd.Util.SceneNavigator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PayOrderController {

    @FXML private VBox detailsBox;
    @FXML private VBox itemsBox;
    @FXML private Button payCardButton;
    @FXML private Button payWalletButton;

    private Order currentOrder;
    private Restaurant restaurant;

    public void setOrder(Order order,Restaurant restaurant) {
        this.currentOrder = order;
        this.restaurant = restaurant;

        // Populate details
        detailsBox.getChildren().addAll(
                new Label("📦 Status: " + order.getStatus()),
                new Label("📍 Delivery Address: " + order.getDeliveryAddress()),
                new Label("👤 Customer ID: " + order.getCustomerId()),
                new Label("🍽 Restaurant : " + restaurant.getName()),
                new Label("🎟 Coupon ID: " + (order.getCouponId() != null ? order.getCouponId() : "None")),
                new Label("💰 Raw Price: " + order.getRawPrice()),
                new Label("💸 Tax Fee: " + order.getTaxFee()),
                new Label("➕ Additional Fee: " + order.getAdditionalFee()),
                new Label("🚚 Courier Fee: " + order.getCourierFee()),
                new Label("💳 Total to Pay: " + order.getPayPrice())
        );

        // Populate order items
        for (OrderItem item : order.getItems()) {
            String itemText = "🍔food id:" + item.getItemId() + " | " + "quantity :" + item.getQuantity();
            Label itemLabel = new Label(itemText);
            itemLabel.setStyle("-fx-font-size: 14px;");
            itemsBox.getChildren().add(itemLabel);
        }
    }

    @FXML
    private void handlePayWithCard() {
        System.out.println("Paying with card for order ID: " + currentOrder.getId());
        sendPaymentRequest("card");
    }

    @FXML
    private void handlePayWithWallet() {
        System.out.println("Paying with wallet for order ID: " + currentOrder.getId());
        sendPaymentRequest("wallet");
    }

    public void handleBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/cart.fxml",payCardButton);
    }
    private void sendPaymentRequest(String method) {
        try {
            URL url = new URL("http://localhost:8080/payment/online");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
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
                handleBack(new ActionEvent());
            } else {
                showAlert("Failed", "Payment failed! Status: " + responseCode, Alert.AlertType.ERROR);
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

}
