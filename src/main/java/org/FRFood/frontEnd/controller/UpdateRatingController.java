package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.FRFood.entity.Restaurant;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class UpdateRatingController {

    @FXML private TextArea commentArea;
    @FXML private HBox starBox;
    @FXML private FlowPane imagePreviewPane;

    private int ratingId;
    private int selectedRating;
    private final List<Button> starButtons = new ArrayList<>();
    private final List<String> base64Images = new ArrayList<>();

    public static class RatingData {
        public int id;
        public int order_id;
        public int rating;
        public String comment;
        public List<String> imageBase64;
    }

    private static RatingData ratingData;

    public static void setRatingData(RatingData data) {
        ratingData = data;
    }

    @FXML
    public void initialize() {
        if (ratingData == null) return;

        this.ratingId = ratingData.id;
        this.selectedRating = ratingData.rating;
        commentArea.setText(ratingData.comment);

        setupStars();
        loadExistingImages(ratingData.imageBase64);
    }

    private void setupStars() {
        for (int i = 1; i <= 5; i++) {
            Button star = new Button("☆");
            star.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #888; -fx-cursor: hand;");
            final int ratingValue = i;

            star.setOnMouseEntered(e -> updateStars(ratingValue));
            star.setOnMouseExited(e -> updateStars(selectedRating));
            star.setOnAction(e -> {
                selectedRating = ratingValue;
                updateStars(selectedRating);
            });

            star.setFocusTraversable(false);
            star.setPadding(new Insets(5));
            starButtons.add(star);
            starBox.getChildren().add(star);
        }
        updateStars(selectedRating);
    }

    private void updateStars(int ratingValue) {
        for (int i = 0; i < starButtons.size(); i++) {
            Button star = starButtons.get(i);
            if (i < ratingValue) {
                star.setText("★");
                star.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: gold; -fx-cursor: hand;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #888; -fx-cursor: hand;");
            }
        }
    }

    private void loadExistingImages(List<String> base64List) {
        if (base64List == null) return;
        for (String encoded : base64List) {
            base64Images.add(encoded);
            addImagePreviewFromBase64(encoded);
        }
    }

    private void addImagePreviewFromBase64(String base64) {
        Image image = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
        ImageView imgView = createRemovableImageView(image, base64);
        imagePreviewPane.getChildren().add(imgView);
    }

    private ImageView createRemovableImageView(Image image, String base64ToRemove) {
        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(80);
        imgView.setFitHeight(80);
        imgView.setStyle("-fx-border-color: #ccc; -fx-cursor: hand;");
        imgView.setOnMouseClicked(e -> {
            base64Images.remove(base64ToRemove);
            imagePreviewPane.getChildren().remove(imgView);
        });
        return imgView;
    }

    @FXML
    private void handleAddImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    String encoded = Base64.getEncoder().encodeToString(bytes);
                    base64Images.add(encoded);
                    addImagePreviewFromBase64(encoded);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Image Error", "Couldn't load: " + file.getName());
                }
            }
        }
    }

    @FXML
    private void handleSubmitUpdate() {
        Map<String, Object> body = new HashMap<>();
        body.put("rating", selectedRating);
        body.put("comment", commentArea.getText());
        body.put("imageBase64", base64Images);

        try {
            URL url = new URL("http://localhost:8080/ratings/" + ratingId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = conn.getResponseCode();
            if (code == 200 || code == 204) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Rating updated!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Update failed. Code: " + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not update rating.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
