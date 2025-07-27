package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ProfileController {

    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
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
                    emailField.setText(root.has("email") && !root.get("email").isNull() && !root.get("email").asText().equals("null")
                            ? root.get("email").asText() : "");
                    addressField.setText(root.get("address").asText());

                    JsonNode bankInfo = root.get("bank_info");
                    if (bankInfo != null) {
                        bankNameField.setText(bankInfo.get("bank_name").asText());
                        accountNumberField.setText(bankInfo.get("account_number").asText());
                    }

                    profileImageBase64 = root.has("profileImageBase64") && !root.get("profileImageBase64").isNull()
                            ? root.get("profileImageBase64").asText()
                            : "";

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
        if (fullNameField.getText().isBlank() || phoneField.getText().isBlank()
                || addressField.getText().isBlank() || bankNameField.getText().isBlank()
                || accountNumberField.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields (except email and password) must be filled.");
            return;
        }

        if (!phoneField.getText().trim().matches("^\\d{7,15}$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number is not valid.");
            return;
        }

        String email = emailField.getText().trim();
        if (!email.isBlank() && !email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address."));
            return;
        }


        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/auth/profile");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                ObjectMapper mapper = new ObjectMapper();

                ObjectNode rootNode = mapper.createObjectNode();
                rootNode.put("full_name", fullNameField.getText().trim());
                rootNode.put("phone", phoneField.getText().trim());
                rootNode.put("email", emailField.getText().trim());
                rootNode.put("address", addressField.getText().trim());
                rootNode.put("profileImageBase64", profileImageBase64);

                ObjectNode bankNode = mapper.createObjectNode();
                bankNode.put("bank_name", bankNameField.getText().trim());
                bankNode.put("account_number", accountNumberField.getText().trim());
                rootNode.set("bank_info", bankNode);

                if (!passwordField.getText().isBlank()) {
                    rootNode.put("password", passwordField.getText().trim());
                }

                String requestBody = mapper.writeValueAsString(rootNode);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes());
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == 200 || responseCode == 204) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated!");
                        handleCancel(event);
                    });
                } else if (responseCode == 400) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number is not valid.");
                } else if (responseCode == 409) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number already exists.");
                } else {
                    String errorMsg = "";
                    try (InputStream errStream = conn.getErrorStream()) {
                        if (errStream != null) {
                            errorMsg = new String(errStream.readAllBytes());
                        }
                    }
                    final String finalMsg = errorMsg;
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            "Update Failed", "HTTP " + responseCode + ": " + finalMsg));
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleDeletePicture(ActionEvent event) {
        profileImage.setImage(null);
        profileImageBase64 = "";
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/panel.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}