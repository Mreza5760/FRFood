package org.FRFood.frontEnd.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.FRFood.frontEnd.Util.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CreteFoodController {
    public TextField nameField;
    public TextField priceField;
    public TextField supplyField;
    public TextField descriptionField;
    public Button submitButton;
    public Button uploadLogoButton;
    public ListView keywordsListView;

    public void goBack(ActionEvent actionEvent) {
    }

    @FXML
    public void initialize() {
        fetchOptions();

    }

    private void fetchOptions() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/getKeywords"))
                .header("Authorization", "Bearer " + SessionManager.getAuthToken())
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            List<String> options = mapper.readValue(response.body(), new TypeReference<>() {});
                            Platform.runLater(() -> {
                                keywordsListView.getItems().addAll(options);
                                keywordsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


}
