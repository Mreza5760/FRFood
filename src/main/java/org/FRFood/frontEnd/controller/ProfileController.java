package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FRFood.frontEnd.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ProfileController {

    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField bankNameField;
    @FXML private TextField accountNumberField;
    @FXML private ImageView profileImage;

    private String profileImageBase64 = "";
    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/auth/profile");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                InputStream responseStream = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseStream);

                Platform.runLater(() -> {
                    fullNameField.setText(root.get("full_name").asText());
                    phoneField.setText(root.get("phone").asText());
                    emailField.setText(root.get("email").asText());
                    addressField.setText(root.get("address").asText());
                    JsonNode bankInfo = root.get("bank_info");
                    bankNameField.setText(bankInfo.get("bank_name").asText());
                    accountNumberField.setText(bankInfo.get("account_number").asText());
                    profileImageBase64 = root.get("profileImageBase64").asText();

                    if (!profileImageBase64.isEmpty()) {
                        byte[] imageBytes = Base64.getDecoder().decode(profileImageBase64);
                        profileImage.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleChangePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageBytes = new FileInputStream(file).readAllBytes();
                profileImageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                profileImage.setImage(new Image(new FileInputStream(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSaveChanges(ActionEvent event) {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/auth/profile");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();
                String requestBody = mapper.createObjectNode()
                        .put("full_name", fullNameField.getText())
                        .put("phone", phoneField.getText())
                        .put("email", emailField.getText())
                        .put("address", addressField.getText())
                        .put("profileImageBase64", profileImageBase64)
                        .set("bank_info", mapper.createObjectNode()
                                .put("bank_name", bankNameField.getText())
                                .put("account_number", accountNumberField.getText())
                        ).toString();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Update response: " + responseCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/Panel.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
