package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WalletController {

    @FXML
    private Label balanceLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Button depositButton;

    @FXML
    private Button withdrawButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button backButton;

    private int balance = 0;
    private final String token = SessionManager.getAuthToken();

    @FXML
    public void initialize() {
        loadWalletBalance();

        depositButton.setOnAction(e -> updateWallet(true));
        withdrawButton.setOnAction(e -> updateWallet(false));
        historyButton.setOnAction(e -> handleHistory());
        backButton.setOnAction(e -> handleBack());  // added back button action
    }

    private void handleBack() {
        SceneNavigator.switchTo("/frontEnd/panel.fxml",backButton);
    }

    private void loadWalletBalance() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/auth/profile");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                InputStream responseStream = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseStream);

                balance = root.get("wallet").asInt();

                Platform.runLater(this::updateBalanceLabel);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateWallet(boolean isDeposit) {
        try {
            int amount = Integer.parseInt(amountField.getText());
            if (!isDeposit && amount > balance) {
                System.out.println("Not enough balance for withdrawal.");
                showAlert(Alert.AlertType.ERROR, "Error", "Not enough balance for withdrawal.");
                return;
            }

            int finalAmount = isDeposit ? amount : -amount;

            new Thread(() -> {
                try {
                    URL url = new URL("http://localhost:8080/wallet/top-up");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    ObjectMapper mapper = new ObjectMapper();
                    String requestBody = mapper.createObjectNode()
                            .put("amount", finalAmount)
                            .toString();

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(requestBody.getBytes());
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        balance += finalAmount;
                        Platform.runLater(() -> {
                            updateBalanceLabel();
                            amountField.clear();
                        });
                    } else {
                        System.out.println("Wallet update failed: " + responseCode + ": " + conn.getResponseMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (NumberFormatException ignored) {}
    }

    private void handleHistory() {
        SceneNavigator.switchTo("/frontEnd/transactions.fxml",historyButton);
        System.out.println("Navigate to history");
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Wallet Balance: " + balance + " Toman");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}