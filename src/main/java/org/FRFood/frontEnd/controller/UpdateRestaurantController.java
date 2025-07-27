package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class UpdateRestaurantController {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField taxFeeField;
    @FXML private TextField additionalFeeField;
    @FXML private ImageView restaurantLogo;

    private int restaurantId;
    private String logoBase64 = "";
    private final String token = SessionManager.getAuthToken();

    public void setRestaurantData(int id, String name, String address, String phone, int taxFee, int additionalFee, String logo) {
        this.restaurantId = id;
        nameField.setText(name);
        addressField.setText(address);
        phoneField.setText(phone);
        taxFeeField.setText(String.valueOf(taxFee));
        additionalFeeField.setText(String.valueOf(additionalFee));
        this.logoBase64 = logo;

        if (logo != null && !logo.isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(logo);
            restaurantLogo.setImage(new Image(new ByteArrayInputStream(imageBytes)));
        }
    }

    @FXML
    public void handleChangeLogo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Logo");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageBytes = new FileInputStream(file).readAllBytes();
                logoBase64 = Base64.getEncoder().encodeToString(imageBytes);
                restaurantLogo.setImage(new Image(new FileInputStream(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String taxFeeStr = taxFeeField.getText().trim();
        String additionalFeeStr = additionalFeeField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || taxFeeStr.isEmpty() || additionalFeeStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Fill all fields", "Fill all fields");
            return;
        }

        if (!phone.matches("^\\+?\\d{10,15}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid phone number", "Invalid phone number");
            return;
        }

        int taxFee = parseIntSafe(taxFeeStr);
        int additionalFee = parseIntSafe(additionalFeeStr);
        if (taxFee < 0 || additionalFee < 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid tax fee or additional fee", "Invalid tax fee or additional fee");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/restaurants/" + restaurantId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();
                String requestBody = mapper.createObjectNode()
                        .put("name", name)
                        .put("address", address)
                        .put("phone", phone)
                        .put("tax_fee", taxFee)
                        .put("additional_fee", additionalFee)
                        .put("logoBase64", logoBase64)
                        .toString();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Restaurant update response: " + responseCode);
                System.out.println(conn.getResponseMessage());

                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        SceneNavigator.switchTo("/frontend/myRestaurants.fxml", (Node) event.getSource());
                    });
                } else if (responseCode == 400) {
                    Platform.runLater(() -> {showAlert(Alert.AlertType.ERROR, "Bad Request", "Invalid phone number");});
                } else {
                    Platform.runLater(() -> {showAlert(Alert.AlertType.ERROR, "Bad Request", "Failed to update restaurant");});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleDeleteLogo(ActionEvent event) {
        restaurantLogo.setImage(null);
        logoBase64 = "";
        showAlert(Alert.AlertType.INFORMATION, "Deleted", "Restaurant logo removed. Click Save to confirm changes.");
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/myRestaurants.fxml", (Node) event.getSource());
    }
}