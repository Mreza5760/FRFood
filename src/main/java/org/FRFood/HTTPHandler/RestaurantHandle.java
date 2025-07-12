package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.DAO.*;
import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.util.JsonResponse;

import java.io.IOException;

public class RestaurantHandle implements HttpHandler {
    private final RestaurantDAO restaurantDAO;
    private final ObjectMapper objectMapper;
    private final UserDAO userDAO;

    public RestaurantHandle() {
        restaurantDAO = new  RestaurantDAOImp();
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        userDAO = new UserDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try{
            if(path.equals("/restaurants")) {
                handleRestaurants();
            }else if(path.equals("/restaurants/mine")) {}
        }catch(Exception e){
            System.out.println(e.getMessage());
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleRestaurants(HttpExchange exchange) throws IOException {
        Restaurant restaurant;
        try {
            restaurant = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
        } catch (Exception e) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Invalid input\"}");
            return;
        }
    }
}
