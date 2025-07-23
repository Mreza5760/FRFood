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
    private Label welcomeLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Button orderFoodButton;
    @FXML
    private Button favoriteRestaurantsButton; // NEW
    @FXML
    private Button walletButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button restaurantButton;
    @FXML
    private Button deliveriesButton;
    @FXML
    private Button addRestaurantButton;

    // Admin-only
    @FXML
    private Button usersButton;
    @FXML
    private Button ordersButton;
    @FXML
    private Button transactionsButton;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        setRoleBasedButtons();

        logoutButton.setOnAction(e -> handleLogout());
        orderFoodButton.setOnAction(e -> handleOrders());
        favoriteRestaurantsButton.setOnAction(e -> handleFavorites());
        addRestaurantButton.setOnAction(e -> handleCreateRestaurant());
        restaurantButton.setOnAction(e -> handleRestaurants());
        walletButton.setOnAction(e -> handleWallet());
        profileButton.setOnAction(e -> handleProfile());

        // Admin placeholders
        usersButton.setOnAction(e -> System.out.println("Users clicked"));
        ordersButton.setOnAction(e -> System.out.println("Orders clicked"));
        transactionsButton.setOnAction(e -> handleTransactions());
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

        switch (user.getRole()) {
            case buyer -> {
                orderFoodButton.setVisible(true);
                orderFoodButton.setManaged(true);
                favoriteRestaurantsButton.setVisible(true);
                favoriteRestaurantsButton.setManaged(true);
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
            case admin -> {
                usersButton.setVisible(true);
                usersButton.setManaged(true);
                ordersButton.setVisible(true);
                ordersButton.setManaged(true);
                transactionsButton.setVisible(true);
                transactionsButton.setManaged(true);

                // Hide Wallet & Profile for Admin
                walletButton.setVisible(false);
                walletButton.setManaged(false);
                profileButton.setVisible(false);
                profileButton.setManaged(false);
            }
        }
    }

    private void handleCreateRestaurant() {
        SceneNavigator.switchTo("/frontend/createRestaurant.fxml", addRestaurantButton);
    }

    private void handleLogout() {
        SessionManager.logout();
        SceneNavigator.switchTo("/frontend/home.fxml", logoutButton);
    }

    private void handleOrders() {
        SceneNavigator.switchTo("/frontEnd/buyerOrderPage.fxml", orderFoodButton);
    }

    private void handleFavorites() {
        SceneNavigator.switchTo("/frontEnd/favorites.fxml", favoriteRestaurantsButton);
        System.out.println("Navigated to Favorite Restaurants");
    }

    private void handleWallet() {
        SceneNavigator.switchTo("/frontEnd/wallet.fxml", walletButton);
    }

    private void handleProfile() {
        SceneNavigator.switchTo("/frontend/profile.fxml", profileButton);
    }

    private void handleRestaurants() {
        SceneNavigator.switchTo("/frontend/myRestaurants.fxml", restaurantButton);
    }

    private  void handleTransactions() {
        SceneNavigator.switchTo("/frontEnd/adminTransactions.fxml", transactionsButton);
    }
}