package org.FRFood.frontEnd.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.FRFood.frontEnd.Util.SceneNavigator;

public class BuyerOrderPageController {

    @FXML
    private Button backButton;
    @FXML
    private Button orderHistoryButton;
    @FXML
    private Button myOrdersButton;
    @FXML
    private Button restaurantButton;


    @FXML
    private void initialize() {
        backButton.setOnAction(event ->
                SceneNavigator.switchTo("/fxml/Panel.fxml", backButton)
        );

        orderHistoryButton.setOnAction(event ->
                handleOrderHistoryButtonClick()
        );
        myOrdersButton.setOnAction(event ->
                SceneNavigator.switchTo("/fxml/cart.fxml", myOrdersButton)
        );
        restaurantButton.setOnAction(event ->
                handleRestaurantButton()
        );

    }

    private void handleRestaurantButton() {
        AllRestaurantsController.setMode(1);
        SceneNavigator.switchTo("/fxml/allRestaurants.fxml", restaurantButton);
    }

    private void handleOrderHistoryButtonClick() {
        OrderHistoryController.setMode(1);
        SceneNavigator.switchTo("/fxml/orderHistory.fxml", orderHistoryButton);
    }
}