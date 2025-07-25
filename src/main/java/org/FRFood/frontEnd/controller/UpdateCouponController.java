package org.FRFood.frontEnd.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.FRFood.entity.Coupon;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class UpdateCouponController {

    @FXML
    private TextField codeField, valueField, minPriceField, userCountField;

    @FXML
    private ComboBox<String> typeBox;

    @FXML
    private DatePicker startDatePicker, endDatePicker;

    private Coupon currentCoupon;

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
    private void handleUpdate(ActionEvent event) {
        try {
            String couponCode = codeField.getText();
            String type = typeBox.getValue();
            int value = Integer.parseInt(valueField.getText());
            int minPrice = Integer.parseInt(minPriceField.getText());
            int userCount = Integer.parseInt(userCountField.getText());
            String startDate = startDatePicker.getValue().toString();
            String endDate = endDatePicker.getValue().toString();

            Map<String, Object> data = new HashMap<>();
            data.put("coupon_code", couponCode);
            data.put("type", type);
            data.put("value", value);
            data.put("min_price", minPrice);
            data.put("user_count", userCount);
            data.put("start_date", startDate);
            data.put("end_date", endDate);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/admin/coupons/" + currentCoupon.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showAlert("Coupon updated successfully.");
                SceneNavigator.switchTo("/frontend/allCoupons.fxml",codeField);
            } else {
                showAlert("Failed to update coupon.\nStatus: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/allCoupons.fxml",codeField);
    }

    private void closeStage() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coupon");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }
}
