package org.FRFood.frontEnd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger logger = Logger.getLogger("javafx.scene.CssStyleHelper");
        logger.setLevel(Level.SEVERE);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/home.fxml"));
        Scene scene = new Scene(loader.load(), 900, 700);

        primaryStage.setTitle("Frfood");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);
        for (var handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.SEVERE);
        }
        Logger logger = Logger.getLogger("javafx.scene.CssStyleHelper");
        logger.setLevel(Level.SEVERE);
        launch(args);
    }
}
