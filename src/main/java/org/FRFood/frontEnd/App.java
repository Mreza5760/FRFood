package org.FRFood.frontEnd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontEnd/signup.fxml"));
        Scene scene = new Scene(loader.load(), 400, 500);

        primaryStage.setTitle("Sign Up");
        primaryStage.setScene(scene);
        primaryStage.show();

//
//        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/frontEnd/login.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//        primaryStage.setTitle("Login | FRFood");
//        primaryStage.setScene(scene);
//        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
