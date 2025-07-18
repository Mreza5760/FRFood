package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.FRFood.DAO.UserDAO;
import org.FRFood.DAO.UserDAOImp;
import org.FRFood.entity.User;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;
import org.FRFood.util.JwtUtil;

import java.sql.SQLException;

public class PanelController {

    @FXML
    public Button addRestaurantButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Button orderFoodButton;
    @FXML
    private Button walletButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button restaurantButton;
    @FXML
    private Button deliveriesButton;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        // Role-based button visibility
        setRoleBasedButtons();

        // Button actions
        logoutButton.setOnAction(e -> handleLogout());
        orderFoodButton.setOnAction(e -> handleOrders());
        addRestaurantButton.setOnAction(e -> handleCreateRestaurant());
        restaurantButton.setOnAction(e -> handleRestaurants());
        walletButton.setOnAction(e -> handleWallet());
        profileButton.setOnAction(e -> handleProfile());
    }

    private void handleRestaurants() {
        SceneNavigator.switchTo("/frontend/MyRestaurants.fxml",restaurantButton);
    }

    private void setRoleBasedButtons() {
        String token = SessionManager.getAuthToken();
        if (token == null) return;

        Jws<Claims> claimsJws = JwtUtil.validateToken(token);
        int userId = Integer.parseInt(claimsJws.getBody().getSubject());

        UserDAO userDao = new UserDAOImp();
        User user = null;
        try {
            user = userDao.getById(userId).orElse(null);
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
        if (user == null) return;

        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");

        // Show only relevant button based on role
        System.out.println("Welcome: " + welcomeLabel.getText());
        switch (user.getRole()) {
            case buyer -> {
                orderFoodButton.setVisible(true);
                orderFoodButton.setManaged(true);
            }
            case seller -> {
                restaurantButton.setVisible(true);
                restaurantButton.setManaged(true);
                addRestaurantButton.setVisible(true);
                addRestaurantButton.setManaged(true);
            }
            case courier -> {
                deliveriesButton.setVisible(true);
                deliveriesButton.setManaged(true);
            }
        }
    }

    private void handleCreateRestaurant() {
        SceneNavigator.switchTo("/frontend/createRestaurant.fxml", addRestaurantButton);
    }

    private void handleLogout() {
        SessionManager.logout();
        SceneNavigator.switchTo("/frontend/home.fxml", logoutButton);
        System.out.println("Logout successful");
    }

    private void handleOrders() {
        System.out.println("Navigating to Orders...");
        // SceneNavigator.switchTo("/frontend/orders.fxml", orderButton);
    }

    private void handleWallet() {
        System.out.println("Wallet clicked (fetch logic can go here)");
        // Future: Fetch wallet API call
    }

    private void handleProfile() {
        SceneNavigator.switchTo("/frontend/profile.fxml", profileButton);
        System.out.println("Navigated to Profile");
    }
}
