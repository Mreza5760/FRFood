package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.FRFood.entity.Restaurant;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class AllRestaurantsController {

    public Button backButton;
    public TextField searchBox1;
    public TextField searchBox2;
    @FXML
    private VBox restaurantList;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> goBack());
        searchBox1.setOnAction(e -> handleSearch1());
        searchBox2.setOnAction(e -> handleSearch2());
        fetchRestaurants();
    }



    private void fetchRestaurants() {
        String tempJson = "{\"search\": \"\" }";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/vendors"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .POST(HttpRequest.BodyPublishers.ofString(tempJson))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        displayRestaurants(response.body());
                    } else {
                        System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayRestaurants(String json) {
        try {
            List<Restaurant> restaurants = mapper.readValue(json, new TypeReference<>() {
            });
            Platform.runLater(() -> {
                restaurantList.getChildren().clear();
                for (Restaurant r : restaurants) {
                    restaurantList.getChildren().add(createRestaurantCard(r));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createRestaurantCard(Restaurant r) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 6);");
        card.setPrefWidth(600);

        // Logo
        ImageView logo = new ImageView();
        try {
            byte[] imageData = Base64.getDecoder().decode(r.getLogo());
            logo.setImage(new Image(new ByteArrayInputStream(imageData)));
        } catch (Exception e) {
            logo.setImage(null); // fallback if needed
        }
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        // Info
        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("📛 " + r.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e2a38; -fx-font-weight: bold;");

        Label addressLabel = new Label("📍 " + r.getAddress());
        addressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label phoneLabel = new Label("📞 " + r.getPhone());
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        Label feeLabel = new Label("💰 Tax: " + r.getTaxFee() + " | Additional Fee: " + r.getAdditionalFee());
        feeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");

        info.getChildren().addAll(nameLabel, addressLabel, phoneLabel, feeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.setOnMouseClicked(e -> handleClick(r)); // full card click

        card.getChildren().addAll(logo, info, spacer);
        return card;
    }

    private void handleClick(Restaurant r) {
        RestaurantController.setValues(r.getId(),r.getName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/restaurant.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = (Stage) restaurantList.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            stage.setScene(new Scene(root));
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        SceneNavigator.switchTo("/frontend/buyerOrderPage.fxml", searchBox1);
    }

    private void handleSearch1() {
    }

    private void handleSearch2() {
    }
}
