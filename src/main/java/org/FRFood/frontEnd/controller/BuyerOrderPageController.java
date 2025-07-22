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
                System.out.println("Order History clicked (not implemented)")
        );
        myOrdersButton.setOnAction(event ->
                System.out.println("My Orders clicked (not implemented)")
        );
        restaurantButton.setOnAction(event ->
                restaurantsHandle()
        );

    }

    private void restaurantsHandle() {
        SceneNavigator.switchTo("/frontEnd/allRestaurants.fxml", restaurantButton);
    }
}