package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.FRFood.frontEnd.SessionManager;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class PanelController {

//    @FXML
//    private Label walletLabel;

    @FXML
    private Button logoutButton, ordersButton, walletButton, profileButton;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        fetchWalletBalance();

        logoutButton.setOnAction(e -> handleLogout());
        ordersButton.setOnAction(e -> handleOrders());
        walletButton.setOnAction(e -> handleWallet());
        profileButton.setOnAction(e -> handleProfile());
    }

    private void fetchWalletBalance() {
        // Example API URL, change to your actual backend URL
//        String url = "http://localhost:8080/user/wallet";
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();

        // Async request so UI thread is not blocked
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(HttpResponse::body)
//                .thenAccept(this::updateWalletBalance)
//                .exceptionally(e -> {
//                    System.err.println("Failed to fetch wallet: " + e.getMessage());
//                    return null;
//                });
    }

//    private void updateWalletBalance(String json) {
//        try {
//            JsonNode node = objectMapper.readTree(json);
//            // Assuming JSON is like { "balance": 123.45 }
//            double balance = node.get("balance").asDouble();
//
//            // Update UI on JavaFX Application Thread
//            javafx.application.Platform.runLater(() ->
//                    walletLabel.setText(String.format("Wallet Balance: $%.2f", balance))
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow(); // see below
            stage.setScene(new Scene(root));
            stage.setTitle("profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) profileButton.getScene().getWindow(); // see below
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        SessionManager.logout();
        goToHome();
        System.out.println("Logout clicked");
        // Add your logout logic here
    }

    private void handleOrders() {
        System.out.println("Orders clicked");
        // Navigate to orders page or fetch orders
    }

    private void handleWallet() {
        System.out.println("Wallet clicked");
        // Possibly refresh wallet info or open wallet detail
        fetchWalletBalance();
    }

    private void handleProfile() {
        goToProfile();
        System.out.println("Profile clicked");
        // Navigate to profile page
    }
}
