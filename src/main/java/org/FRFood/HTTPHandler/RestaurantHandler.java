package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import static org.FRFood.util.Role.*;
import static org.FRFood.util.Validation.validatePhoneNumber;

import java.util.List;
import java.sql.ResultSet;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class RestaurantHandler implements HttpHandler {
    private final FoodDAO foodDAO;
    private final ObjectMapper objectMapper;
    private final RestaurantDAO restaurantDAO;

    public RestaurantHandler() {
        foodDAO = new FoodDAOImp();
        restaurantDAO = new RestaurantDAOImp();
        objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST" -> {
                    if (path.equals("/restaurants")) handleRestaurants(exchange);
                    else if (path.matches("^/\\d+/item$")) addItem(exchange);
                    else if (path.matches("^/\\d+/menu$")) addMenu(exchange);
                }
                case "GET" -> {
                    if (path.equals("/restaurants/mine")) myRestaurants(exchange);
                    else if (path.matches("^/\\d+/orders$")) getOrders(exchange);
                }
                case "PUT" -> {
                    if (path.matches("^/\\d+$")) handleUpdateRestaurants(exchange);
                    else if (path.matches("^/\\d+/item/\\d+$")) editItem(exchange);
                    else if (path.matches("^/\\d+/menu/[^/]+$")) addItemToMenu(exchange);
                }
                case "DELETE" -> {
                    if (path.matches("^/\\d+/item/\\d+$")) deleteItem(exchange);
                    else if (path.matches("^/\\d+/menu/[^/]+$")) deleteMenu(exchange);
                    else if (path.matches("^/\\d+/menu/[^/]+/\\d+$")) deleteItemFromMenu(exchange);
                }
                case "PATCH" -> {
                    if (path.matches("^/orders/\\d+$")) setStatus(exchange);
                }
                default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            HttpError.internal(exchange, "Unexpected server error: " + e.getMessage());
        }
    }

    private void handleRestaurants(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        try {
            Restaurant restaurant = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
            if (!user.getRole().equals(seller)) {
                HttpError.forbidden(exchange, "Only sellers can register restaurants");
                return;
            }

            // TODO Tax fee should be more than zero
            if (restaurant.getName() == null || restaurant.getAddress() == null || restaurant.getPhone() == null || restaurant.getTaxFee() == 0 || restaurant.getAdditionalFee() == 0) {
                HttpError.badRequest(exchange, "Missing required fields");
                return;
            }

            if (!validatePhoneNumber(restaurant.getPhone())) {
                HttpError.unsupported(exchange, "Invalid phone number");
                return;
            }

            restaurant.setId(restaurantDAO.insert(restaurant, user.getId()));
            JsonResponse.sendJsonResponse(exchange, 201, objectMapper.writeValueAsString(restaurant));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to register restaurant");
        }
    }

    private void myRestaurants(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only seller can access their restaurants");
            return;
        }

        // TODO Should add to the restaurantDao, i dont check this shit
        String query = "SELECT * FROM Restaurants WHERE owner_id = ?";
        try (Connection conn = DBConnector.gConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Restaurant restaurant = new Restaurant(user,
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("logo"),
                        rs.getInt("tax_fee"),
                        rs.getInt("additional_fee"));
                restaurant.setId(rs.getInt("id"));
                JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(restaurant));
            } else {
                HttpError.notFound(exchange, "No restaurant found for user");
            }
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to retrieve restaurant");
        }
    }

    private void handleUpdateRestaurants(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can update restaurants");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }

            Restaurant updated = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
            updated.setId(restaurantId);
            restaurantDAO.Update(updated);
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(updated));
        } catch (Exception e) {
            HttpError.internal(exchange, "Failed to update restaurant");
        }
    }

    private void addItem(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can add items");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);
        Food food = objectMapper.readValue(exchange.getRequestBody(), Food.class);
        food.setRestaurantId(restaurantId);

        // TODO default val
        if (food.getName() == null || food.getDescription() == null || food.getPrice() == 0 || food.getSupply() == 0 || food.getKeywords() == null) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }
            food.setId(foodDAO.insert(food));
            JsonResponse.sendJsonResponse(exchange, 201, objectMapper.writeValueAsString(food));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to add food item");
        }
    }

    private void editItem(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can edit items");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);
        int foodId = Integer.parseInt(parts[4]);
        Food food = objectMapper.readValue(exchange.getRequestBody(), Food.class);
        food.setRestaurantId(restaurantId);
        food.setId(foodId);

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }
            foodDAO.update(food);
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(food));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to edit food item");
        }
    }

    private void deleteItem(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can delete items");
            return;
        }

        int foodId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[4]);
        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }

            foodDAO.delete(foodId);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Food item removed successfully\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to delete food item");
        }
    }

    private void addMenu(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can add menus");
            return;
        }

        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        Menu menu = objectMapper.readValue(exchange.getRequestBody(), Menu.class);

        if (menu.getTitle() == null) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }
            menu.setRestaurant(restaurant);
            menu.setId(restaurantDAO.insertMenu(menu));
            JsonResponse.sendJsonResponse(exchange, 201, objectMapper.writeValueAsString(menu));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to add menu");
        }
    }

    private void deleteMenu(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can delete menu");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);
        String menuTitle = parts[4];

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }
            restaurantDAO.deleteMenuByTitle(menuTitle, restaurantId);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Menu deleted successfully\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to delete menu");
        }
    }

    private void addItemToMenu(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can add item to menu");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);
        JsonNode node = objectMapper.readTree(exchange.getRequestBody());
        int itemId = node.get("item_id").asInt();
        String title = parts[4];

        if (itemId == 0) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        try {
            Restaurant restaurant = restaurantDAO.getById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            if (restaurant.getOwner().getId() != user.getId()) {
                HttpError.unauthorized(exchange, "You do not own this restaurant");
                return;
            }
            Menu menu = restaurantDAO.getMenuByTitle(title, restaurantId)
                    .orElseThrow(() -> new RuntimeException("Menu not found"));
            foodDAO.addFood(itemId, menu.getId());
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Food item added to menu\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to add item to menu");
        }
    }

    private void deleteItemFromMenu(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can delete item from menu");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int foodId = Integer.parseInt(parts[5]);

        try {
            foodDAO.setMenuIdNull(foodId);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Item removed from menu\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to remove item from menu");
        }
    }

    private void getOrders(HttpExchange exchange) throws IOException {
        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            List<Order> orders = new OrderDAOImp().getRestaurantOrders(restaurantId);
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to get restaurant orders");
        }
    }

    private void setStatus(HttpExchange exchange) throws IOException {
        int orderId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[3]);
        String status = objectMapper.readTree(exchange.getRequestBody()).get("status").asText();
        try {
            new OrderDAOImp().changeStatus(orderId, status);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Order status updated\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to update order status");
        }
    }
}