package org.FRFood.frontEnd.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.FRFood.frontEnd.Util.SceneNavigator;

import java.io.IOException;

public class HomeController {

    @FXML private Button loginButton;
    @FXML private Button signupButton;

    @FXML
    public void goToLogin(ActionEvent event) throws IOException {
        SceneNavigator.switchTo("/frontEnd/Login.fxml", loginButton);
    }

    @FXML
    public void goToSignup(ActionEvent event) throws IOException {
        SceneNavigator.switchTo("/frontEnd/signup.fxml", signupButton);
    }
}