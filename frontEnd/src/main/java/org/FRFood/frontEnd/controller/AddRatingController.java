package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class AddRatingController {

    @FXML private TextArea commentArea;
    @FXML private FlowPane imagePreviewPane;
    @FXML private HBox starBox;

    private final List<String> base64Images = new ArrayList<>();
    private final List<Button> starButtons = new ArrayList<>();
    private int selectedRating = 0;
    private static int orderId;

    public static void setOrderId(int InOrderId) {
        orderId = InOrderId;
    }

    @FXML
    public void initialize() {
        setupStars();
    }

    private void setupStars() {
        starBox.getChildren().clear();
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
        updateStars(0);
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
                    addImagePreview(file, encoded);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Image Error", "Couldn't load: " + file.getName());
                }
            }
        }
    }

    private void addImagePreview(File file, String encoded) {
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString(), 80, 80, true, true);
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
            imgView.setFitWidth(80);
            imgView.setFitHeight(80);
            imgView.setStyle("-fx-border-color: #ccc; -fx-border-radius: 6; -fx-background-radius: 6;");

            Button removeBtn = new Button("✖");
            removeBtn.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            VBox wrapper = new VBox(imgView, removeBtn);
            wrapper.setSpacing(5);
            wrapper.setAlignment(javafx.geometry.Pos.CENTER);
            wrapper.setPadding(new Insets(5));

            removeBtn.setOnAction(e -> {
                base64Images.remove(encoded);
                imagePreviewPane.getChildren().remove(wrapper);
            });

            imagePreviewPane.getChildren().add(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitReview() {
        String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";

        if (selectedRating <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Rating", "Please select a star rating.");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("order_id", orderId);
        body.put("rating", selectedRating);
        body.put("comment", comment);
        body.put("imageBase64", base64Images);

        try {
            URL url = new URL("http://localhost:8080/ratings");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getAuthToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = conn.getResponseCode();
            if (code == 200 || code == 201) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Review submitted!");
                    SceneNavigator.switchTo("/frontEnd/orderHistory.fxml", imagePreviewPane);
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed. Code: " + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not send review.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void handleBack(javafx.event.ActionEvent event) {
        SceneNavigator.switchTo("/frontEnd/orderHistory.fxml", imagePreviewPane);
    }
}