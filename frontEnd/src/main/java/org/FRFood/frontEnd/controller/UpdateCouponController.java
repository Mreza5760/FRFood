package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UpdateCouponController {

    @FXML private TextField codeField, valueField, minPriceField, userCountField;
    @FXML private ComboBox<String> typeBox;
    @FXML private DatePicker startDatePicker, endDatePicker;

    private Coupon currentCoupon;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setCoupon(Coupon coupon) {
        this.currentCoupon = coupon;

        codeField.setText(coupon.getCouponCode());
        typeBox.setItems(FXCollections.observableArrayList("fixed", "percent"));
        typeBox.setValue(coupon.getType().toString());
        valueField.setText(String.valueOf(coupon.getValue()));
        minPriceField.setText(String.valueOf(coupon.getMinPrice()));
        userCountField.setText(String.valueOf(coupon.getUserCount()));
        startDatePicker.setValue(LocalDate.parse(coupon.getStartDate()));
        endDatePicker.setValue(LocalDate.parse(coupon.getEndDate()));
    }

    @FXML
    private void handleUpdate() {
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
            if (code.contains(" ")) {
                showAlert(AlertType.ERROR, "Invalid Code", "Coupon Code must not contain spaces.");
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
                if (type.equals("percent") && value >= 100) {
                    showAlert(AlertType.ERROR, "Invalid Numbers", "Value can’t be greater than or equal 100 for percent type.");
                    return;
                }
                if (type.equals("fixed") && minPrice <= value) {
                    showAlert(AlertType.ERROR, "Invalid Numbers", "Value must be less than min price for fixed type.");
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

            Map<String, Object> data = new HashMap<>();
            data.put("coupon_code", code);
            data.put("type", type);
            data.put("value", value);
            data.put("min_price", minPrice);
            data.put("user_count", userCount);
            data.put("start_date", startDate.toString());
            data.put("end_date", endDate.toString());

            String requestBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/admin/coupons/" + currentCoupon.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            Platform.runLater(() -> {
                                showAlert(AlertType.INFORMATION, "Success", "Coupon updated successfully!");
                                SceneNavigator.switchTo("/frontEnd/allCoupons.fxml", codeField);
                            });
                        } else if (status == 403) {
                            showAlert(AlertType.ERROR, "Duplicate Coupon", "This coupon code already exists.");
                        } else {
                            showAlert(AlertType.ERROR, "Failed", "Server error: HTTP " + status);
                        }
                    })
                    .exceptionally(ex -> {
                        showAlert(AlertType.ERROR, "Error", "Failed to update coupon: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
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