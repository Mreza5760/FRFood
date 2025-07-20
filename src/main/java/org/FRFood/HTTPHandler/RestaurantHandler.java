package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;

import static org.FRFood.util.Role.*;
import static org.FRFood.util.Validation.validatePhone;
import static org.FRFood.util.Validation.validatePhoneNumber;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class RestaurantHandler implements HttpHandler {
    private final FoodDAO foodDAO;
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;
    private final RestaurantDAO restaurantDAO;

    public RestaurantHandler() {
        foodDAO = new FoodDAOImp();
        orderDAO = new OrderDAOImp();
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
                    else if (path.matches("^/restaurants/\\d+/item$")) addItem(exchange);
                    else if (path.matches("^/restaurants/\\d+/menu$")) addMenu(exchange);
                }
                case "GET" -> {
                    if (path.equals("/restaurants/mine")) myRestaurants(exchange);
                    else if (path.matches("^/\\d+/orders$")) getOrders(exchange);
                    else if (path.matches("^/restaurants/\\d+/menus$")) getMenus(exchange);
                    else if (path.matches("^/\\d+/items/[^/]+$")) getMenuItems(exchange);
                    else if (path.matches("^/restaurants/\\d+/menu/[^/]+$")) getItemsOutOfMenu(exchange);
                    else if (path.matches("^/restaurants/keywords$")) getKeywords(exchange);
                }
                case "PUT" -> {
                    if (path.matches("^/restaurants/\\d+$")) handleUpdateRestaurants(exchange);
                    else if (path.matches("^/\\d+/item/\\d+$")) editItem(exchange);
                    else if (path.matches("^/\\d+/menu/[^/]+$")) addItemToMenu(exchange);
                }
                case "DELETE" -> {
                    if (path.matches("^/restaurants/\\d+$")) deleteRestaurant(exchange);
                    else if (path.matches("^/\\d+/item/\\d+$")) deleteItem(exchange);
                    else if (path.matches("^/restaurants/\\d+/menu/[^/]+$")) deleteMenu(exchange);
                    else if (path.matches("^restaurants/\\d+/menu/[^/]+/\\d+$")) deleteItemFromMenu(exchange);
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

            if (restaurant.getName() == null || restaurant.getAddress() == null || restaurant.getPhone() == null || restaurant.getTaxFee() == null || restaurant.getAdditionalFee() == null) {
                HttpError.badRequest(exchange, "Missing required fields");
                return;
            }

            if (!validatePhone(restaurant.getPhone())) {
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

        try {
            if (!restaurantDAO.getByOwnerId(user.getId()).isEmpty()) {
                JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(restaurantDAO.getByOwnerId(user.getId())));
            } else
                HttpError.badRequest(exchange, "Own no restaurant");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to get restaurant by owner");
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
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;

            Restaurant updated = objectMapper.readValue(exchange.getRequestBody(), Restaurant.class);
            updated.setId(restaurantId);
            if (!Validation.validatePhone(updated.getPhone())) {
                HttpError.badRequest(exchange, "Invalid phone number");
                return;
            }
            restaurantDAO.Update(updated);
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(updated));
        } catch (Exception e) {
            HttpError.internal(exchange, "Failed to update restaurant" + e.getMessage());
        }
    }

    private void deleteRestaurant(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can delete restaurants");
            return;
        }

        try {
            Authenticate.restaurantChecker(exchange, user, restaurantId);
            restaurantDAO.DeleteById(restaurantId);
            JsonResponse.sendJsonResponse(exchange, 200, "success");
        } catch (Exception e) {
            HttpError.internal(exchange, "Failed to delete restaurant");
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

        KeywordDAO keywordDAO = new KeywordDAOImp();
        List<Keyword> thekeywods = new ArrayList<>();
        for (Keyword keyword : food.getKeywords()) {
            try {
                thekeywods.add(keywordDAO.getKeywordByName(keyword.getName()).orElse(null));
            } catch (SQLException e) {
                HttpError.badRequest(exchange, "error !!");
            }
        }
        food.setKeywords(thekeywods);
        if (food.getName() == null || food.getDescription() == null || food.getPrice() == null || food.getSupply() == null || food.getKeywords() == null) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        try {
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Restaurant restaurant = restaurantOpt.get();
            System.out.println("debug");
            food.setId(foodDAO.insert(food));
            System.out.println("debug");
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
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Restaurant restaurant = restaurantOpt.get();

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
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Restaurant restaurant = restaurantOpt.get();

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
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            menu.setRestaurant(restaurantOpt.get());
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
        String menuTitle = URLDecoder.decode(parts[4], StandardCharsets.UTF_8);

        try {
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;

            if (restaurantDAO.getMenuByTitle(menuTitle, restaurantId).isEmpty()) {
                HttpError.notFound(exchange, "Menu title not found");
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
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Optional<Menu> optionalMenu = restaurantDAO.getMenuByTitle(title, itemId);
            if (optionalMenu.isEmpty()) {
                HttpError.notFound(exchange, "Menu title not found");
                return;
            }
            Menu menu = optionalMenu.get();

            foodDAO.addFoodToMenu(menu.getId(), itemId);
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
        String menuTitle = parts[4];
        int foodId = Integer.parseInt(parts[5]);
        int restaurantId = Integer.parseInt(parts[2]);

        try {
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Optional<Menu> optionalMenu = restaurantDAO.getMenuByTitle(menuTitle, restaurantId);
            if (optionalMenu.isEmpty()) {
                HttpError.notFound(exchange, "Menu title not found");
                return;
            }
            Menu menu = optionalMenu.get();

            foodDAO.deleteMenuItem(menu.getId(), foodId);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Item removed from menu\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to remove item from menu");
        }
    }

    // TODO can have query
    private void getOrders(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can get orders");
            return;
        }

        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);

        try {
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;

            List<Order> orders = orderDAO.getRestaurantOrders(restaurantId);
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to get restaurant orders");
        }
    }

    private void setStatus(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can set status");
            return;
        }

        int orderId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[3]);
        JsonNode jsonNode = objectMapper.readTree(exchange.getRequestBody());
        if (!jsonNode.hasNonNull("status")) {
            HttpError.badRequest(exchange, "Missing required field: status");
            return;
        }
        String input = jsonNode.get("status").asText();
        Status status = Status.valueOf(input);
        if (status != Status.cancelled && status != Status.findingCourier && status != Status.preparing) {
            HttpError.badRequest(exchange, "Order status is not valid");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.getById(orderId);
            if (optionalOrder.isEmpty()) {
                HttpError.notFound(exchange, "Order not found");
                return;
            }
            Order order = optionalOrder.get();
            int restaurantId = order.getRestaurantId();
            var restaurantOpt = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (restaurantOpt.isEmpty()) return;
            Restaurant restaurant = restaurantOpt.get();

            if (!order.getStatus().equals(Status.waiting) && (order.getStatus().equals(Status.preparing)) && status != Status.findingCourier) {
                HttpError.badRequest(exchange, "Order status is not valid");
                return;
            }

            if (status == Status.cancelled) {
                Optional<User> optionalCustomer = new UserDAOImp().getById(order.getCustomerId());
                if (optionalCustomer.isEmpty()) return;
                User customer = optionalCustomer.get();
                new UserDAOImp().setWallet(customer.getId(), customer.getWallet() + order.getPayPrice());
            } else if (status == Status.preparing) {
                Optional<User> optionalOwner = new UserDAOImp().getById(restaurant.getOwner().getId());
                if (optionalOwner.isEmpty()) return;
                User owner = optionalOwner.get();
                new UserDAOImp().setWallet(owner.getId(), owner.getWallet() + order.getPayPrice() - order.getCourierFee());
            }

            orderDAO.changeStatus(orderId, status);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Order status updated\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to update order status");
        }
    }

    private void getMenuItems(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can get menu items");
            return;
        }

        String title = exchange.getRequestURI().getPath().split("/")[4];
        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            Optional<Restaurant> optionalRestaurant = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (optionalRestaurant.isEmpty()) return;
            Optional<Menu> optionalMenu = restaurantDAO.getMenuByTitle(title, restaurantId);
            if (optionalMenu.isEmpty()) {
                HttpError.notFound(exchange, "Menu title not found");
                return;
            }
            Menu menu = optionalMenu.get();

            List<Food> menuFoods = restaurantDAO.getMenuFood(restaurantId, menu.getId());
            String json = objectMapper.writeValueAsString(menuFoods);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Failed to update order status");
        }
    }

    private void getItemsOutOfMenu(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can get items");
            return;
        }
        String title = URLDecoder.decode(exchange.getRequestURI().getPath().split("/")[4], StandardCharsets.UTF_8);
        int restaurantId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
        try {
            Optional<Restaurant> optionalRestaurant = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (optionalRestaurant.isEmpty()) return;
            Optional<Menu> optionalMenu = restaurantDAO.getMenuByTitle(title, restaurantId);
            if (optionalMenu.isEmpty()) {
                HttpError.notFound(exchange, "Menu title not found");
                return;
            }
            Menu menu = optionalMenu.get();

            List<Food> allFoods = restaurantDAO.getFoods(restaurantId);
            List<Food> menuFoods = restaurantDAO.getMenuFood(restaurantId, menu.getId());
            allFoods.removeIf(menuFoods::contains);
            String json = objectMapper.writeValueAsString(allFoods);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal error");
        }
    }

    private void getMenus(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can get menus");
            return;
        }

        String[] parts = exchange.getRequestURI().getPath().split("/");
        int restaurantId = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> optionalRestaurant = Authenticate.restaurantChecker(exchange, user, restaurantId);
            if (optionalRestaurant.isEmpty()) return;

            List<Menu> menus = restaurantDAO.getMenus(restaurantId);
            String json = objectMapper.writeValueAsString(menus);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal error");
        }
    }

    private void getKeywords(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(seller)) {
            HttpError.unauthorized(exchange, "Only sellers can get keywords");
            return;
        }

        try {
            List<Keyword> keywords = new KeywordDAOImp().getAllKeywords();
            String json = objectMapper.writeValueAsString(keywords);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal error");
        }
    }
}