package org.FRFood.frontEnd.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.Node;
import org.FRFood.frontEnd.Util.SceneNavigator;
import org.FRFood.frontEnd.Util.SessionManager;

import java.io.IOException;
import java.util.Objects;

public class HomeController {

    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private Button panelButton;

    // Replace with your real token checker logic
    private boolean isAuthenticated() {
        // Example: check if token is saved in a file or global variable
        return SessionManager.getAuthToken() != null;
    }

    @FXML
    public void initialize() {
        boolean auth = isAuthenticated();
        loginButton.setVisible(!auth);
        signupButton.setVisible(!auth);
        panelButton.setVisible(auth);
    }

    @FXML
    public void goToLogin(ActionEvent event) throws IOException {
        SceneNavigator.switchTo("/frontEnd/Login.fxml",loginButton);
    }

    @FXML
    public void goToSignup(ActionEvent event) throws IOException {
        SceneNavigator.switchTo("/frontEnd/signup.fxml",signupButton);
    }

    @FXML
    public void goToPanel(ActionEvent event) throws IOException {
        SceneNavigator.switchTo("/frontEnd/panel.fxml",panelButton);
    }




}
