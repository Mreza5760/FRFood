package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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

public class OrderHistoryController {

    @FXML
    public TextField searchField;
    @FXML
    public TextField vendorIdField;
    @FXML
    public VBox buyerFilterList;
    public TextField adminSearchField;
    public TextField adminVendorField;
    public TextField courierIdField;
    public TextField userIdField;
    public ComboBox statusComboBox;
    public VBox adminFilterList;
    @FXML
    private VBox restaurantList;

    private final ObjectMapper mapper = new ObjectMapper();

    private static int mode;

    public static void setMode(int mode) {
        OrderHistoryController.mode = mode;
    }

    @FXML
    public void initialize() {
        if (mode == 1) {
            buyerFilterList.setVisible(true);
            buyerFilterList.setManaged(true);
        } else if (mode == 5){
            statusComboBox.getItems().addAll("", "waiting", "preparing", "cancelled", "findingCourier", "onTheWay", "completed");
            adminFilterList.setVisible(true);
            adminFilterList.setManaged(true);
        }
        fetchOrders();
    }

    private void fetchOrders() {
        String uri = null;
        if (mode == 1) {
             uri = "http://localhost:8080/orders/history?" +
                    "search=" + searchField.getText().trim() +
                    "&vendor=" + vendorIdField.getText().trim();
        }
        else if (mode == 2) {
            uri = "http://localhost:8080/deliveries/available";
        } else if (mode == 3) {
            uri = "http://localhost:8080/deliveries/order";
        } else if (mode == 4) {
            uri = "http://localhost:8080/deliveries/history";
        } else if (mode == 5) {
            uri = "http://localhost:8080/admin/orders?" +
                    "status=" + statusComboBox.getValue() +
                    "&search=" + adminSearchField.getText().trim() +
                    "&customer=" + userIdField.getText().trim() +
                    "&vendor=" + adminVendorField.getText().trim() +
                    "&courier=" + courierIdField.getText().trim();
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        displayRestaurants(response.body());
                    } else {
                        System.err.println("Failed to fetch orders: HTTP " + response.statusCode() + response.body());
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void displayRestaurants(String body) {
        try {
            List<Order> orders = mapper.readValue(body, new TypeReference<>() {
            });
            List<java.util.concurrent.CompletableFuture<Restaurant>> futures = new ArrayList<>();

            for (Order order : orders) {
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
                                restaurantList.getChildren().add(createRestaurantCard(r, getOrderWithRestaurant(r, orders)));
                            }
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Order getOrderWithRestaurant(Restaurant r, List<Order> orders) {
        for (Order o : orders) {
            if (o.getRestaurantId().equals(r.getId())) {
                orders.remove(o);
                return o;
            }
        }

        return null;
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
                        System.err.println("Failed to fetch restaurant: HTTP " + response.statusCode() + response.body());
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

        card.setOnMouseClicked(e -> handleClick(r, order));


        Label temp = new Label("💰 raw price: " + order.getRawPrice() + " | Total: " + order.getPayPrice() + " | Status: " + order.getStatus());
        temp.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a3a3a;");
        HBox rightBox = new HBox(10, temp);
        rightBox.setAlignment(Pos.CENTER_RIGHT);


        card.getChildren().addAll(logo, info, spacer, rightBox);
        return card;
    }

    private void handleClick(Restaurant r, Order theOrder) {
        PayOrderController controller = SceneNavigator.switchToWithController(
                "/frontEnd/payOrder.fxml",
                restaurantList,
                PayOrderController.class
        );

        if (controller != null) {
            if (mode == 2 || mode == 3 || mode == 4 || mode == 5) {
                controller.setOrder(theOrder, r, 4);
            } else {
                controller.setOrder(theOrder, r, 2);
            }
        }
    }

    @FXML
    private void goBack() {
        if (mode == 2 || mode == 3 || mode == 4 || mode == 5) {
            SceneNavigator.switchTo("/frontEnd/panel.fxml", restaurantList);
        } else {
            SceneNavigator.switchTo("/frontEnd/buyerOrderPage.fxml", restaurantList);
        }
    }

    public void handleSearch(ActionEvent actionEvent) {
        fetchOrders();
    }
}