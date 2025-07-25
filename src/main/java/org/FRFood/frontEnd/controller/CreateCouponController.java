package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
            String code = codeField.getText();
            String type = typeBox.getValue();
            int value = Integer.parseInt(valueField.getText());
            int minPrice = Integer.parseInt(minPriceField.getText());
            int userCount = Integer.parseInt(userCountField.getText());
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (code == null || type == null || startDate == null || endDate == null) {
                showAlert(AlertType.ERROR, "Invalid Input", "Please fill in all fields.");
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
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            showAlert(AlertType.INFORMATION, "Success", "Coupon created successfully!");
                        } else {
                            showAlert(AlertType.ERROR, "Failed", "Server responded with status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        showAlert(AlertType.ERROR, "Error", "Failed to create coupon: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchTo("/frontend/allCoupons.fxml",codeField);
    }

    private void showAlert(AlertType type, String title, String message) {
        // JavaFX thread-safe alert
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
