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
import org.FRFood.entity.Keyword;
import org.FRFood.entity.Rate;
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
import java.security.cert.PolicyNode;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FoodDetailsController {

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

                InputStream is = conn.getInputStream();
                JsonNode root = mapper.readTree(is);

                System.out.println(conn.getResponseCode() + conn.getResponseMessage());
                Platform.runLater(() -> {
                    itemNameLabel.setText(root.get("name").asText());
                    itemDescriptionLabel.setText(root.get("description").asText());
                    itemPriceLabel.setText("$" + root.get("price").asDouble());
                    itemSupplyLabel.setText(String.valueOf(root.get("supply").asInt()));

                    List<Keyword> keywords;
                    try {
                        keywords = mapper.readValue(
                                root.get("keywords").traverse(),
                                new TypeReference<List<Keyword>>() {
                                });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String keys = "";
                    for (Keyword temp : keywords){
                        keys += temp.getName() + ",";
                    }
                    keys = keys.substring(0, keys.length() - 1);
                    itemKeywordsLabel.setText(keys);

                    String imageBase64 = root.get("imageBase64").asText();
                    if (!imageBase64.isEmpty()) {
                        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                        itemImage.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                    }
                });

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

                InputStream is = conn.getInputStream();
                JsonNode root = mapper.readTree(is);

                Platform.runLater(() -> {
                    itemAvgRating.setText(root.get("avg_rating").asText());
                    rateListContainer.getChildren().clear();
                    for (JsonNode comment : root.get("comments")) {
                        Rate rate = mapper.convertValue(comment, Rate.class);
                        showRateCard(rate);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showRateCard(Rate rate) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);");
        card.setMaxWidth(600);
        card.setPrefWidth(600);

        // Top row: Rating + Buttons
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_RIGHT);
        topRow.setSpacing(10);
        topRow.setPrefWidth(600);

        Label ratingLabel = new Label("⭐ Rating: " + rate.getRating());
        ratingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox.setHgrow(ratingLabel, Priority.ALWAYS);

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #00aa88; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        updateButton.setOnAction(e -> {
            handleUpdateButton(rate);
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #cc4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        deleteButton.setOnAction(e -> {
            handleDeleteButton(rate);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        isOwnedByCurrentUser(rate.getOrderId()).thenAccept(isOwner -> {
            if (isOwner) {
                Platform.runLater(() -> {
                    topRow.getChildren().addAll(ratingLabel, spacer, updateButton, deleteButton);
                });
            } else {
                topRow.getChildren().addAll(ratingLabel, spacer);
            }
        });

        // Comment
        Label commentLabel = new Label("📝 " + rate.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 14px;");

        // Created At
        Label createdAtLabel = new Label("📅 " + rate.getCreatedAt());
        createdAtLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        // Images
        FlowPane imagePane = new FlowPane(10, 10);
        imagePane.setPadding(new Insets(10, 0, 0, 0));

        Set<String> uniqueImages = new LinkedHashSet<>(rate.getImages());
        for (String base64 : uniqueImages) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-border-radius: 8;");
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        card.getChildren().addAll(topRow, commentLabel, createdAtLabel, imagePane);

        // Add to container
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
        SceneNavigator.switchTo("/frontend/updateRating.fxml", itemDescriptionLabel);
    }

    private void handleDeleteButton(Rate rate) {
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/ratings/" + rate.getId()))
                    .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                    .DELETE()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        fetchReviews();
                    } else {
                        System.err.println("Failed to fetch: HTTP " + response.statusCode() + " " + response.body());
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
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        return true;
                    } else if (response.statusCode() == 469) {
                        return false;
                    } else {
                        System.err.println("Failed to fetch: HTTP " + response.statusCode() + " " + response.body());
                        return false;
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return false;
                });
    }


    public void handleBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/menu.fxml", itemNameLabel);
    }
}
