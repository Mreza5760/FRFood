package org.FRFood.frontEnd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.FRFood.frontEnd.Util.SessionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger logger = Logger.getLogger("javafx.scene.CssStyleHelper");
        logger.setLevel(Level.SEVERE);

        SessionManager.loadSession();
        FXMLLoader loader = null;
        if (SessionManager.isLoggedIn()) {
            loader = new FXMLLoader(getClass().getResource("/fxml/panel.fxml"));
        } else {
            loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        }
        Scene scene = new Scene(loader.load(), 900, 700);

        primaryStage.setTitle("FRFood");
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