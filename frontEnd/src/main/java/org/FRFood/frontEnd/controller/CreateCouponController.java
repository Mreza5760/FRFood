package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CreateCouponController {

    @FXML private TextField codeField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField valueField;
    @FXML private TextField minPriceField;
    @FXML private TextField userCountField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void initialize() {
        typeBox.getItems().addAll("fixed", "percent");
    }

    @FXML
    private void handleCreate() {
        try {
            String code = codeField.getText() != null ? codeField.getText().trim() : "";
            String type = typeBox.getValue();
            String valueText = valueField.getText() != null ? valueField.getText().trim() : "";
            String minPriceText = minPriceField.getText() != null ? minPriceField.getText().trim() : "";
            String userCountText = userCountField.getText() != null ? userCountField.getText().trim() : "";
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (code.isEmpty() || type == null || valueText.isEmpty() || minPriceText.isEmpty() || userCountText.isEmpty()
                    || startDate == null || endDate == null) {
                showAlert(AlertType.ERROR, "Invalid Input", "All fields must be filled in.");
                return;
            }

            int value, minPrice, userCount;
            try {
                value = Integer.parseInt(valueText);
                minPrice = Integer.parseInt(minPriceText);
                userCount = Integer.parseInt(userCountText);
                if (value <= 0 || minPrice < 0 || userCount <= 0) {
                    showAlert(AlertType.ERROR, "Invalid Numbers", "Value, Min Price, and User Count must be positive.");
                    return;
                }
                if (type.equals("percent") && value > 100) {
                    showAlert(AlertType.ERROR, "Invalid Numbers", "Value cant be greater than 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Invalid Numbers", "Please enter valid numeric values.");
                return;
            }

            if (endDate.isBefore(startDate)) {
                showAlert(AlertType.ERROR, "Invalid Dates", "End Date cannot be before Start Date.");
                return;
            }

            Map<String, Object> couponData = new HashMap<>();
            couponData.put("coupon_code", code);
            couponData.put("type", type);
            couponData.put("value", value);
            couponData.put("min_price", minPrice);
            couponData.put("user_count", userCount);
            couponData.put("start_date", startDate.toString());
            couponData.put("end_date", endDate.toString());

            String requestBody = objectMapper.writeValueAsString(couponData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/admin/coupons"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200 || status == 201) {
                            Platform.runLater(() -> {
                                showAlert(AlertType.INFORMATION, "Success", "Coupon created successfully!");
                                SceneNavigator.switchTo("/frontEnd/allCoupons.fxml", codeField);
                            });
                        } else if (status == 403) {
                            showAlert(AlertType.ERROR, "Duplicate Coupon", "This coupon code already exists.");
                        } else {
                            showAlert(AlertType.ERROR, "Failed", "Server error: HTTP " + status);
                        }
                    })
                    .exceptionally(ex -> {
                        showAlert(AlertType.ERROR, "Error", "Failed to create coupon: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchTo("/frontEnd/allCoupons.fxml", codeField);
    }

    private void showAlert(AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}