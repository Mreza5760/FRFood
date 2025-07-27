package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.FRFood.entity.User;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;


public class PanelController {

    public Button activeOrdersButton;

    public Button deliveryHistoryButton;
    @FXML
    public Button couponsButton;
    @FXML
    public Button topOffers;
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
        deliveriesButton.setOnAction(e -> handleDeliveriesButton());
        activeOrdersButton.setOnAction(e -> handleActiveOrders());
        deliveryHistoryButton.setOnAction(e -> handleDeliveriesHistory());
        topOffers.setOnAction(e -> handleTopOffers());
        // Admin placeholders
        usersButton.setOnAction(e -> handleAllUsers());
        ordersButton.setOnAction(e -> adminOrderButtonClick());
        transactionsButton.setOnAction(e -> handleTransactions());
        couponsButton.setOnAction(e -> handleCouponsButton());
    }

    private void handleTopOffers() {
        SceneNavigator.switchTo("/frontend/topOffers.fxml",restaurantButton);
    }

    private void handleCouponsButton() {
        SceneNavigator.switchTo("/frontend/allCoupons.fxml",restaurantButton);
    }

    private void adminOrderButtonClick() {
        OrderHistoryController.setMode(5);
        SceneNavigator.switchTo("/frontend/orderHistory.fxml",restaurantButton);
    }

    private void handleDeliveriesHistory() {
        OrderHistoryController.setMode(4);
        SceneNavigator.switchTo("/frontend/orderHistory.fxml",restaurantButton);
    }

    private void handleActiveOrders() {
        OrderHistoryController.setMode(3);
        SceneNavigator.switchTo("/frontend/orderHistory.fxml",restaurantButton);
    }

    private void handleDeliveriesButton() {
        OrderHistoryController.setMode(2);
        SceneNavigator.switchTo("/frontend/orderHistory.fxml",restaurantButton);
    }

    private void setRoleBasedButtons() {
        User user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");

        switch (user.getRole()) {
            case buyer -> {
                orderFoodButton.setVisible(true);
                orderFoodButton.setManaged(true);
                favoriteRestaurantsButton.setVisible(true);
                favoriteRestaurantsButton.setManaged(true);
                topOffers.setVisible(true);
                topOffers.setManaged(true);
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
                activeOrdersButton.setVisible(true);
                activeOrdersButton.setManaged(true);
                deliveryHistoryButton.setVisible(true);
                deliveryHistoryButton.setManaged(true);
            }
            case admin -> {
                usersButton.setVisible(true);
                usersButton.setManaged(true);
                ordersButton.setVisible(true);
                ordersButton.setManaged(true);
                transactionsButton.setVisible(true);
                transactionsButton.setManaged(true);
                couponsButton.setVisible(true);
                couponsButton.setManaged(true);
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
        AllRestaurantsController.setMode(2);
        SceneNavigator.switchTo("/frontEnd/allRestaurants.fxml", favoriteRestaurantsButton);
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

    private void handleTransactions() {
        SceneNavigator.switchTo("/frontEnd/adminTransactions.fxml", transactionsButton);
    }

    private void handleAllUsers() {
        SceneNavigator.switchTo("/frontEnd/allUsers.fxml", usersButton);
    }
}