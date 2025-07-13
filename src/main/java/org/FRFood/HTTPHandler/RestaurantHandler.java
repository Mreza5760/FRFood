package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.DAO.*;
import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.util.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.FRFood.util.Role.*;

public class RestaurantHandler implements HttpHandler {
    private final RestaurantDAO restaurantDAO;
    private final ObjectMapper objectMapper;
    private final UserDAO userDAO;

    public RestaurantHandler() {
        restaurantDAO = new RestaurantDAOImp();
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        userDAO = new UserDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            String[] parts = path.split("/");
            if(parts.length == 3){
                handleUpdateRestaurants(exchange,Integer.parseInt(parts[2]));
            }
            if (path.equals("/restaurants")) {
                handleRestaurants(exchange);
            } else if (path.equals("/restaurants/mine")) {
                myRestaurants(exchange);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleRestaurants(HttpExchange exchange) throws IOException {
        if(Authenticate.authenticate(exchange).isEmpty()){
            return;
        }
        User currentUser = Authenticate.authenticate(exchange).get();
        Restaurant restaurant;
        try {
            restaurant = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
            Validate.validatePhone(restaurant.getPhone());
            Validate.validateName(restaurant.getName());
            // needs more validation checks
            if (!currentUser.getRole().equals(buyer)) {
                JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized request\"}");
                return;
            }
            int restaurantId = restaurantDAO.insert(restaurant, currentUser.getId());
            restaurant.setId(restaurantId);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("id", restaurantId);
            result.put("name", restaurant.getName());
            result.put("address", restaurant.getAddress());
            result.put("phone", restaurant.getPhone());
            result.put("logoBase64", restaurant.getLogo());
            result.put("tax_fee", restaurant.getTaxFee());
            result.put("additional_fee", restaurant.getAdditionalFee());
            JsonResponse.sendJsonResponse(exchange, 201, result.toString());
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
        } catch (DataValidationException e) {
            JsonResponse.sendJsonResponse(exchange, 400, e.getMessage());
        }
    }

    private void myRestaurants(HttpExchange exchange) throws IOException {
        if(Authenticate.authenticate(exchange).isEmpty()){
            return;
        }
        User currentUser = Authenticate.authenticate(exchange).get();
        if (!currentUser.getRole().equals(buyer)) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized request\"}");
            return;
        }
        Restaurant restaurant = new Restaurant();
        String statement = "SELECT * FROM restaurants WHERE owner_id = ?";
        try (
                Connection connection = DBConnector.gConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setInt(1, currentUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    restaurant = new Restaurant(currentUser,
                            resultSet.getString("name"),
                            resultSet.getString("address"),
                            resultSet.getString("phone"),
                            resultSet.getString("logo"),
                            resultSet.getInt("tax_fee"),
                            resultSet.getInt("additional_fee"));
                    restaurant.setId(resultSet.getInt("id"));
                }
            }

            ObjectNode result = objectMapper.createObjectNode();
            result.put("id", restaurant.getId());
            result.put("name", restaurant.getName());
            result.put("address", restaurant.getAddress());
            result.put("phone", restaurant.getPhone());
            result.put("logo", restaurant.getLogo());
            result.put("tax_fee", restaurant.getTaxFee());
            result.put("additional_fee", restaurant.getAdditionalFee());
            JsonResponse.sendJsonResponse(exchange, 200, result.toString());
        } catch (SQLException e1) {
            System.out.println(e1.getMessage());
        } catch (DataValidationException e) {
            JsonResponse.sendJsonResponse(exchange, 400, e.getMessage());
        }
    }

    private void handleUpdateRestaurants(HttpExchange exchange,int restaurantId ) throws IOException {
        if(Authenticate.authenticate(exchange).isEmpty()){
            return;
        }
        User currentUser = Authenticate.authenticate(exchange).get();
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();
        Restaurant restaurant = new  Restaurant();
        try {
            if(restaurantDAO.getById(restaurantId).isPresent()){
                restaurant = restaurantDAO.getById(restaurantId).get();
            }else{
                JsonResponse.sendJsonResponse(exchange, 404, "{\"error\":\"Restaurant not found\"}");
            }
        }catch (Exception e1){
            System.out.println(e1.getMessage());
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
        if(restaurant.getOwner().getId() != currentUser.getId()){
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized request\"}");
        }
        try {
            Restaurant newRestaurant = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
            newRestaurant.setId(restaurantId);
            newRestaurant.setName(restaurant.getName());
            Validate.validatePhone(newRestaurant.getPhone());
            Validate.validateName(newRestaurant.getName());
            //needs more validations
            restaurantDAO.UpdateById(newRestaurant);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("id", restaurantId);
            result.put("name", restaurant.getName());
            result.put("address", restaurant.getAddress());
            result.put("phone", restaurant.getPhone());
            result.put("logoBase64", restaurant.getLogo());
            result.put("tax_fee", restaurant.getTaxFee());
            result.put("additional_fee", restaurant.getAdditionalFee());
            JsonResponse.sendJsonResponse(exchange, 201, result.toString());

        }catch (DataValidationException e1){
            JsonResponse.sendJsonResponse(exchange, 400, e1.getMessage());
        }catch (Exception e2){
            System.out.println(e2.getMessage());
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }

}
