package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class FoodDetailsController {

    public Label itemAvgRating;
    @FXML private Label itemNameLabel;
    @FXML private ImageView itemImage;
    @FXML private Label itemDescriptionLabel;
    @FXML private Label itemPriceLabel;
    @FXML private Label itemSupplyLabel;
    @FXML private VBox reviewsContainer;

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

                System.out.println(conn.getResponseCode() + conn.getResponseMessage());
                Platform.runLater(() -> {
                    itemAvgRating.setText(root.get("avg_rating").asText());
                    reviewsContainer.getChildren().clear();
                    for (JsonNode comment : root.get("comments")) {
                        VBox reviewBox = new VBox(5);
                        reviewBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10; -fx-background-radius: 8;");
                        Label rating = new Label("⭐ " + comment.get("rating").asInt());
                        rating.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        Text commentText = new Text(comment.get("comment").asText());
                        commentText.setWrappingWidth(750);
                        reviewBox.getChildren().addAll(rating, commentText);
                        reviewsContainer.getChildren().add(reviewBox);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/menu.fxml",itemNameLabel);
    }
}
