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


            double tempx = 900;
            double tempy = 700;
            stage.setWidth(tempx);
            stage.setHeight(tempy);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading: " + fxmlPath);
        }
    }
}
