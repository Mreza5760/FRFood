package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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

    // This will be called externally to pre-load data
    public void setRestaurantData(int id, String name, String address, String phone, double taxFee, double additionalFee, String logo) {
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
                        .put("name", nameField.getText())
                        .put("address", addressField.getText())
                        .put("phone", phoneField.getText())
                        .put("tax_fee", Double.parseDouble(taxFeeField.getText()))
                        .put("additional_fee", Double.parseDouble(additionalFeeField.getText()))
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
                        // Go back to restaurant list
                        SceneNavigator.switchTo("/frontend/myRestaurants.fxml", (Node) event.getSource());
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        SceneNavigator.switchTo("/frontend/myRestaurants.fxml", (Node) event.getSource());
    }
}
