package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class AllRestaurantsController {

    public Button backButton;
    public TextField searchBox1;
    public TextField searchBox2;
    public TextField minPrice;
    public TextField maxPrice;
    public Button searchInFood;
    public Button searchInRestaurant;
    @FXML
    private VBox restaurantList;

    @FXML
    private RangeSlider priceRangeSlider;
    @FXML
    private Label priceLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    private static int mode;

    public static void setMode(int mode) {
        AllRestaurantsController.mode = mode;
    }

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> goBack());
        priceRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> updatePriceLabel());
        priceRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> updatePriceLabel());
        if (mode == 2 || mode == 3) {
            minPrice.setVisible(false);
            minPrice.setManaged(false);
            maxPrice.setVisible(false);
            maxPrice.setManaged(false);
            searchInFood.setVisible(false);
            searchInFood.setManaged(false);
            searchInRestaurant.setVisible(false);
            searchInRestaurant.setManaged(false);
            searchBox1.setVisible(false);
            searchBox1.setManaged(false);
            searchBox2.setManaged(false);
            searchBox2.setVisible(false);
            fetchRestaurants2();
        } else {
            searchInRestaurant.setOnAction(e -> handleSearch1());
            searchInFood.setOnAction(e -> handleSearch2());
            fetchRestaurants("", null);
        }
    }

    void fetchRestaurants2() {
        List<Restaurant> restaurants;
        if (mode == 2) {
            restaurants = getFavoriteRestaurants();
        }else if(mode == 3){
            restaurants = getBestRestaurants();
        } else {
            restaurants = null;
        }
        Platform.runLater(() -> {
            restaurantList.getChildren().clear();
            for (Restaurant r : restaurants) {
                restaurantList.getChildren().add(createRestaurantCard(r));
            }
        });
    }

    private void fetchRestaurants(String searchString, String[] keywords) {
        StringBuilder keywordsJsonArray = null;
        String tempJson = null;
        if (keywords != null) {
            keywordsJsonArray = new StringBuilder("[");
            for (int i = 0; i < keywords.length; i++) {
                keywordsJsonArray.append("\"").append(keywords[i]).append("\"");
                if (i < keywords.length - 1) {
                    keywordsJsonArray.append(", ");
                }
            }
            keywordsJsonArray.append("]");
            tempJson = "{ \"search\": \"" + searchString.trim() + "\", \"keywords\": " + keywordsJsonArray + " }";
        } else {
            tempJson = "{ \"search\": \"" + searchString.trim() + "\"}";
        }


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

        card.setOnMouseClicked(e -> handleClick(r));


        final boolean[] isFavorite = {false};
        for (Restaurant restaurant : getFavoriteRestaurants()) {
            if (restaurant.getId().equals(r.getId())) {
                isFavorite[0] = true;
                break;
            }
        }

        Label starLabel = new Label(isFavorite[0] ? "★" : "☆");
        starLabel.setFont(Font.font("Arial Unicode MS", FontWeight.BOLD, 26));
        starLabel.setTextFill(Color.GOLD);
        starLabel.setCursor(Cursor.HAND);

        starLabel.setOnMouseClicked(e -> {
            e.consume();
            if (isFavorite[0]) {
                handleAddToFavorites(r);
                starLabel.setText("☆");
            } else {
                handleRemoveFromFavorites(r);
                starLabel.setText("★");
            }
            isFavorite[0] = !isFavorite[0];
        });

        card.getChildren().addAll(logo, info, spacer, starLabel);
        return card;
    }

    private void handleClick(Restaurant r) {
        RestaurantController.setValues(r);
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
        if (mode == 2)
            SceneNavigator.switchTo("/frontEnd/panel.fxml", backButton);
        else if(mode ==1)
            SceneNavigator.switchTo("/frontEnd/buyerOrderPage.fxml", backButton);
        else if(mode == 3)
            SceneNavigator.switchTo("/frontEnd/topOffers.fxml", backButton);
    }

    private void handleSearch1() {
        String temp = searchBox2.getText().trim();
        if (!temp.isEmpty()) {
            String[] parts = temp.split(",");
            fetchRestaurants(searchBox1.getText(), parts);
        } else {
            fetchRestaurants(searchBox1.getText(), null);
        }
    }

    private void handleSearch2() {
        String name = searchBox1.getText().trim();


        int minPriceValue = (int) priceRangeSlider.getLowValue();
        int maxPriceValue = (int) priceRangeSlider.getHighValue();


        String temp = searchBox2.getText().trim();
        String[] parts = temp.split(",");
        List<String> keywords = Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        ItemsReq itemsReq = new ItemsReq();
        itemsReq.search = name;
        itemsReq.minPrice = minPriceValue;
        itemsReq.maxPrice = maxPriceValue;
        itemsReq.keywords = keywords;
        AllFoodsController.setData(0, "Foods :", 2, itemsReq);
        SceneNavigator.switchTo("/frontEnd/allFoods.fxml", restaurantList);
    }

    private List<Restaurant> getFavoriteRestaurants() {
        List<Restaurant> favoriteRestaurants = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/favorites"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                favoriteRestaurants = mapper.readValue(response.body(), new TypeReference<List<Restaurant>>() {
                });
            } else {
                System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return favoriteRestaurants;
    }

    private List<Restaurant> getBestRestaurants() {
        List<Restaurant> bestRestaurants = new ArrayList<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/top/restaurants"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                bestRestaurants = mapper.readValue(response.body(), new TypeReference<List<Restaurant>>() {
                });
            } else {
                System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bestRestaurants;
    }

    private void handleRemoveFromFavorites(Restaurant restaurant) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/favorites/" + restaurant.getId()))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode() + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddToFavorites(Restaurant restaurant) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/favorites/" + restaurant.getId()))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch restaurants: HTTP " + response.statusCode() + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePriceLabel() {
        int min = (int) priceRangeSlider.getLowValue();
        int max = (int) priceRangeSlider.getHighValue();
        priceLabel.setText(min + " - " + max);
    }
}