package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private int selectedRating = 5;
    private static int orderId;

    public static void setOrderId(int InOrderId) {
        orderId = InOrderId;
    }

    @FXML
    public void initialize() {
        setupStars();
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

            star.setFocusTraversable(false); // prevents keyboard focus border
            star.setPadding(new Insets(5));
            starButtons.add(star);
            starBox.getChildren().add(star);
        }
        updateStars(selectedRating); // show initial rating
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
                    addImagePreview(file);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Image Error", "Couldn't load: " + file.getName());
                }
            }
        }
    }

    private void addImagePreview(File file) {
        try {
            Image img = new Image(file.toURI().toString(), 80, 80, true, true);
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(80);
            imgView.setFitHeight(80);
            imgView.setStyle("-fx-border-color: #ccc; -fx-border-radius: 6; -fx-background-radius: 6;");
            imagePreviewPane.getChildren().add(imgView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitReview() {
        String comment = commentArea.getText();

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
                showAlert(Alert.AlertType.INFORMATION, "Success", "Review submitted!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed. Code: " + code+ conn.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not send review.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleBack(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontend/orderHistory.fxml",imagePreviewPane);
    }
}
