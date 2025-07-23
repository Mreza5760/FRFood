package org.FRFood.frontEnd.Util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    public static void switchTo(String fxmlPath, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();


            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            stage.setScene(new Scene(root));


            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading: " + fxmlPath);
        }
    }
    public static <T> T switchToWithController(String fxmlPath, Node sourceNode, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();

            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            stage.setScene(new Scene(root));
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.show();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading: " + fxmlPath);
            return null;
        }
    }
}