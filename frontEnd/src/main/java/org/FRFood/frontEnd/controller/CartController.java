package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CartController {

    @FXML
    private VBox restaurantList;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        displayRestaurants();
    }

    private void displayRestaurants() {
        try {
            Map<Integer, Order> orderList = SessionManager.getOrderList();
            Set<Integer> keys = orderList.keySet();
            List<java.util.concurrent.CompletableFuture<Restaurant>> futures = new ArrayList<>();
            for (Integer key : keys) {
                Order order = orderList.get(key);
                int restaurantId = order.getRestaurantId();
                futures.add(fetchRestaurant(restaurantId));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        List<Restaurant> restaurants = new ArrayList<>();
                        for (CompletableFuture<Restaurant> future : futures) {
                            try {
                                Restaurant r = future.get();
                                if (r != null) {
                                    restaurants.add(r);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Platform.runLater(() -> {
                            restaurantList.getChildren().clear();
                            for (Restaurant r : restaurants) {
                                restaurantList.getChildren().add(createRestaurantCard(r, orderList.get(r.getId())));
                            }
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Restaurant> fetchRestaurant(int restaurantId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/vendors/" + restaurantId))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode root = mapper.readTree(response.body());
                            JsonNode menuTitles = root.get("vendor");
                            return mapper.readValue(menuTitles.toString(), Restaurant.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Failed to fetch restaurant: HTTP " + response.statusCode());
                    }
                    return null;
                });
    }

    private HBox createRestaurantCard(Restaurant r, Order order) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 6);");
        card.setPrefWidth(600);

        ImageView logo = new ImageView();
        try {
            byte[] imageData = Base64.getDecoder().decode(r.getLogo());
            logo.setImage(new Image(new ByteArrayInputStream(imageData)));
        } catch (Exception e) {
            logo.setImage(null);
        }
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

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

        card.setOnMouseClicked(e -> handleClick(r,order));


        Label temp = new Label("💰 raw price: " + order.getRawPrice() + " | Total: " + order.getPayPrice());
        temp.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");
        HBox rightBox = new HBox(10, temp);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleClick(Restaurant r,Order theOrder) {
        PayOrderController controller = SceneNavigator.switchToWithController(
                "/frontEnd/payOrder.fxml",
                restaurantList,
                PayOrderController.class
        );

        if (controller != null) {
            controller.setOrder(theOrder, r,1);
        }
    }

    @FXML
    private void goBack() {
        SceneNavigator.switchTo("/frontEnd/buyerOrderPage.fxml", restaurantList);
    }
}