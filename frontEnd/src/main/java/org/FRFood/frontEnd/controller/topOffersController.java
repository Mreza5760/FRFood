package org.FRFood.frontEnd.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.FRFood.frontEnd.entity.*;
import org.FRFood.frontEnd.Util.SceneNavigator;

public class topOffersController {
    public Button foodsButton;

    public void bestFoodsHandler(ActionEvent actionEvent) {
        MenuController.setData("Top Foods",new Restaurant(),2);
        SceneNavigator.switchTo("/frontEnd/menu.fxml",foodsButton);
    }

    public void bestRestaurantsHandler(ActionEvent actionEvent) {
        AllRestaurantsController.setMode(3);
        SceneNavigator.switchTo("/frontEnd/allRestaurants.fxml",foodsButton);
    }

    public void handleCancel(ActionEvent actionEvent) {
        SceneNavigator.switchTo("/frontEnd/panel.fxml",foodsButton);
    }
}