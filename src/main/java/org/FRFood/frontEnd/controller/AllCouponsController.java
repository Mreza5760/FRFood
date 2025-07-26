package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.FRFood.entity.Coupon;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.URI;
import java.net.http.*;
import java.util.List;

public class AllCouponsController {

    @FXML
    private VBox couponContainer;

    @FXML
    private Button backButton;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadCoupons();
    }

    private void loadCoupons() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/admin/coupons"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Coupon> coupons = mapper.readValue(json, new TypeReference<>() {});
                        Platform.runLater(() -> showCoupons(coupons));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void showCoupons(List<Coupon> coupons) {
        couponContainer.getChildren().clear();

        for (Coupon coupon : coupons) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.1, 0, 2);");
            card.setMaxWidth(600);

            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,150,136,0.4), 15, 0.3, 0, 4); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.1, 0, 2); -fx-scale-x: 1; -fx-scale-y: 1;"));

            card.setOnMouseClicked(event -> handleCardClick(coupon));

            Label codeLabel = new Label("Code: " + coupon.getCouponCode());
            Label typeLabel = new Label("Type: " + coupon.getType());
            Label valueLabel = new Label("Value: " + coupon.getValue());
            Label minLabel = new Label("Min Price: " + coupon.getMinPrice());
            Label userCountLabel = new Label("User Count: " + coupon.getUserCount());
            Label dateLabel = new Label("Valid: " + coupon.getStartDate() + " ➜ " + coupon.getEndDate());

            for (Label label : List.of(codeLabel, typeLabel, valueLabel, minLabel, userCountLabel, dateLabel)) {
                label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            }

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
            deleteButton.setOnAction(e -> {
                e.consume();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Confirmation");
                alert.setHeaderText("Are you sure you want to delete this coupon?");
                alert.setContentText("Code: " + coupon.getCouponCode());

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        handleDeleteCoupon(coupon);
                    }
                });
            });

            HBox buttonContainer = new HBox(deleteButton);
            buttonContainer.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(codeLabel, typeLabel, valueLabel, minLabel, userCountLabel, dateLabel, buttonContainer);
            couponContainer.getChildren().add(card);
        }
    }

    private void handleCardClick(Coupon coupon) {
        UpdateCouponController controller = SceneNavigator.switchToWithController(
                "/frontend/updateCoupon.fxml",
                backButton,
                UpdateCouponController.class
        );

        if (controller != null) {
            controller.setCoupon(coupon);
        }
    }

    private void handleDeleteCoupon(Coupon coupon) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/admin/coupons/" + coupon.getId()))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Coupon Deleted");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Coupon \"" + coupon.getCouponCode() + "\" has been successfully deleted.");
                            successAlert.showAndWait();
                            loadCoupons();
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Delete Failed");
                            errorAlert.setHeaderText("Failed to delete coupon");
                            errorAlert.setContentText("HTTP " + response.statusCode() + ": " + response.body());
                            errorAlert.showAndWait();
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Unexpected error occurred");
                        errorAlert.setContentText(e.getMessage());
                        errorAlert.showAndWait();
                    });
                    return null;
                });
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchTo("/frontend/panel.fxml",backButton);
    }

    public void handleAddCoupon(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/createCoupon.fxml",backButton);
    }
}
