package org.FRFood.frontEnd.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
        // Navigate back to the panel
        backButton.setOnAction(event ->
                SceneNavigator.switchTo("/frontEnd/Panel.fxml", backButton)
        );

        // Placeholder actions
        orderHistoryButton.setOnAction(event ->
                SceneNavigator.switchTo("/frontEnd/orderHistory.fxml", orderHistoryButton)
        );
        myOrdersButton.setOnAction(event ->
                SceneNavigator.switchTo("/frontEnd/cart.fxml", myOrdersButton)
        );
        restaurantButton.setOnAction(event ->
                SceneNavigator.switchTo("/frontEnd/allRestaurants.fxml", restaurantButton)
        );

    }


}