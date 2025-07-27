package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FoodDetailsController {

    @FXML
    public Label itemAvgRating;
    @FXML
    public VBox rateListContainer;
    @FXML
    public Label itemKeywordsLabel;
    @FXML
    private Label itemNameLabel;
    @FXML
    private ImageView itemImage;
    @FXML
    private Label itemDescriptionLabel;
    @FXML
    private Label itemPriceLabel;
    @FXML
    private Label itemSupplyLabel;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String token = SessionManager.getAuthToken();
    private static int itemId;

    public static void setItemId(int id) {
        itemId = id;
    }

    @FXML
    private void initialize() {
        fetchReviews();
        fetchItemDetails();
    }

    private void fetchItemDetails() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/items/" + itemId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = mapper.readTree(is);

                    Platform.runLater(() -> {
                        itemNameLabel.setText(root.get("name").asText());
                        itemDescriptionLabel.setText(root.get("description").asText());
                        itemPriceLabel.setText(root.get("price").asInt() + " Toman");
                        itemSupplyLabel.setText(String.valueOf(root.get("supply").asInt()));

                        List<Keyword> keywords;
                        try {
                            keywords = mapper.readValue(
                                    root.get("keywords").traverse(),
                                    new TypeReference<>() {});
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String keys = String.join(", ", keywords.stream().map(Keyword::getName).toList());
                        itemKeywordsLabel.setText(keys);

                        // Decode and display image
                        String imageBase64 = root.get("imageBase64").asText();
                        if (!imageBase64.isEmpty()) {
                            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                            itemImage.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchReviews() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/ratings/items/" + itemId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = mapper.readTree(is);

                    Platform.runLater(() -> {
                        double avg = root.get("avg_rating").asDouble();
                        itemAvgRating.setText(String.format("%.1f", avg));
                        rateListContainer.getChildren().clear();

                        for (JsonNode comment : root.get("comments")) {
                            Rate rate = mapper.convertValue(comment, Rate.class);
                            showRateCard(rate);
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showRateCard(Rate rate) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);
            """);
        card.setMaxWidth(700);
        card.setPrefWidth(700);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label("⭐ " + rate.getRating());
        ratingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
        HBox.setHgrow(ratingLabel, Priority.ALWAYS);

        Button updateButton = new Button("Update");
        updateButton.setStyle("""
            -fx-background-color: #00b894;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        updateButton.setOnAction(e -> handleUpdateButton(rate));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        deleteButton.setOnAction(e -> handleDeleteButton(rate));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        isOwnedByCurrentUser(rate.getOrderId()).thenAccept(isOwner -> {
            Platform.runLater(() -> {
                if (isOwner) {
                    topRow.getChildren().addAll(ratingLabel, spacer, updateButton, deleteButton);
                } else {
                    topRow.getChildren().addAll(ratingLabel, spacer);
                }
            });
        });

        Label commentLabel = new Label(rate.getComment().isBlank() ? "(No Comment)" : "📝 " + rate.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        Label createdAtLabel = new Label("📅 " + rate.getCreatedAt());
        createdAtLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        FlowPane imagePane = new FlowPane(10, 10);
        imagePane.setPadding(new Insets(10, 0, 0, 0));

        Set<String> uniqueImages = new LinkedHashSet<>(rate.getImages());
        for (String base64 : uniqueImages) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(120);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        card.getChildren().addAll(topRow, commentLabel, createdAtLabel, imagePane);
        rateListContainer.getChildren().add(card);
    }

    private void handleUpdateButton(Rate rating) {
        UpdateRatingController.RatingData data = new UpdateRatingController.RatingData();
        data.id = rating.getId();
        data.order_id = rating.getOrderId();
        data.rating = rating.getRating();
        data.comment = rating.getComment();
        data.imageBase64 = rating.getImages();

        UpdateRatingController.setRatingData(data);
        SceneNavigator.switchTo("/frontEnd/updateRating.fxml", itemDescriptionLabel);
    }

    private void handleDeleteButton(Rate rate) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/ratings/" + rate.getId()))
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .DELETE()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        fetchReviews();
                    } else {
                        System.err.println("Failed: HTTP " + response.statusCode() + " " + response.body());
                    }
                    return null;
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<Boolean> isOwnedByCurrentUser(int orderId) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/ratings/user/" + orderId))
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .GET()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200 || response.statusCode() == 204)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return false;
                });
    }

    public void handleBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontEnd/menu.fxml", itemNameLabel);
    }
}