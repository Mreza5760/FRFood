package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;

import static org.FRFood.util.Role.*;
import org.FRFood.util.BuyerReq.ItemsReq;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.util.BuyerReq.VendorsReq;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;

import java.util.List;
import java.util.Random;
import java.util.Optional;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BuyerHandler implements HttpHandler {
    private final FoodDAO foodDAO;
    private final UserDAO userDAO;
    private final RateDAO rateDAO;
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;
    private final RestaurantDAO restaurantDAO;

    public BuyerHandler() {
        foodDAO = new FoodDAOImp();
        userDAO = new UserDAOImp();
        rateDAO = new RateDAOImp();
        orderDAO = new OrderDAOImp();
        objectMapper = new ObjectMapper();
        restaurantDAO = new RestaurantDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/vendors" -> handleVendorsList(exchange);
                        case "/items" -> handleItemsList(exchange);
                        case "/orders" -> handleSubmitOrder(exchange);
                        case "/ratings" -> handeSubmitRate(exchange);
                        default -> HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "GET" -> {
                    if (path.matches("^/vendors/\\d+$")) handleVendorsMenu(exchange);
                    else if (path.matches("^/items/\\d+$")) handleGetItem(exchange);
                    else if (path.equals("/orders/history")) handleOrdersHistory(exchange);
                    else if (path.equals("/favorites")) handleGetFavorites(exchange);
                    else if (path.matches("^/ratings/items/\\d+$")) handeGetFoodRates(exchange);
                    else if (path.matches("^/orders/\\d+$")) handleGetOrder(exchange);
                    else if (path.matches("^/ratings/\\d+$")) handleGetRate(exchange);
                    else HttpError.notFound(exchange, "Not Found");
                }
                case "PUT" -> {
                    if (path.matches("^/favorites/\\d+$")) handleInsertFavorite(exchange);
                    else if (path.matches("^/ratings/\\d+$")) handleUpdateRate(exchange);
                    else HttpError.notFound(exchange, "Not Found");
                }
                case "DELETE" -> {
                    if (path.matches("^/favorites/\\d+$")) handleDeleteFavorite(exchange);
                    else if (path.matches("^/ratings/\\d+$")) handleDeleteRate(exchange);
                    else HttpError.notFound(exchange, "Not Found");
                }
                default -> HttpError.notFound(exchange, "Not Found");
            }

        } catch (Exception e) {
            HttpError.internal(exchange, "Internal Server Error");
        }
    }

    private void handleVendorsList(HttpExchange exchange) throws IOException {
        if (authenticate(exchange).isEmpty()) return;

        VendorsReq req;
        req = objectMapper.readValue(exchange.getRequestBody(), VendorsReq.class);

        try {
            List<Restaurant> restaurantsFiltered = new ArrayList<>();
            List<Restaurant> restaurants = restaurantDAO.searchByString(req.search);
            for (Restaurant restaurant : restaurants) {
                boolean haveFood = false;
                List<Food> foods = restaurantDAO.getFoods(restaurant.getId());
                for (Food food : foods) {
                    if (foodDAO.doesHaveKeywords(req.keywords, food.getId())) {
                        haveFood = true;
                        break;
                    }
                }
                if (haveFood) {
                    restaurantsFiltered.add(restaurant);
                }
            }

            String json = objectMapper.writeValueAsString(restaurantsFiltered);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error");
        }
    }

    private void handleVendorsMenu(HttpExchange exchange) throws IOException {
        if (authenticate(exchange).isEmpty()) return;

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(id);
            if (optionalRestaurant.isEmpty()) {
                HttpError.notFound(exchange, "Restaurant not found");
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();
            List<Menu> menus = restaurantDAO.getMenus(restaurant.getId());
            List<Food> foods = restaurantDAO.getFoods(restaurant.getId());

            ObjectNode root = objectMapper.createObjectNode();
            JsonNode vendorNode = objectMapper.valueToTree(restaurant);
            root.set("vendor", vendorNode);

            ArrayNode menuTitlesArray = objectMapper.createArrayNode();
            for (Menu menu : menus) {
                menuTitlesArray.add(menu.getTitle());
            }
            root.set("menu_titles", menuTitlesArray);

            JsonNode foodArrayNode = objectMapper.valueToTree(foods);
            root.set("menu_title", foodArrayNode);

            String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            JsonResponse.sendJsonResponse(exchange, 200, jsonOutput);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error");
        }
    }

    private void handleItemsList(HttpExchange exchange) throws IOException {
        if (authenticate(exchange).isEmpty()) return;

        ItemsReq req = objectMapper.readValue(exchange.getRequestBody(), ItemsReq.class);

        try {
            List<Food> foodsFiltered = new ArrayList<>();
            List<Food> foods = foodDAO.searchFood(req.search);
            for (Food food : foods) {
                if (foodDAO.doesHaveKeywords(req.keywords, food.getId()) && food.getPrice() <= req.price) {
                    foodsFiltered.add(food);
                }
            }
            String json = objectMapper.writeValueAsString(foodsFiltered);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error");
        }
    }

    private void handleGetItem(HttpExchange exchange) throws IOException {
        if (authenticate(exchange).isEmpty()) return;

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Food> optionalFood = foodDAO.getById(id);
            if (optionalFood.isEmpty()) {
                HttpError.notFound(exchange, "Food not found");
                return;
            }

            Food food = optionalFood.get();
            String json = objectMapper.writeValueAsString(food);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error");
        }
    }

    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can submit orders");
            return;
        }

        Order order = objectMapper.readValue(exchange.getRequestBody(), Order.class);

        if (order.getDeliveryAddress() == null || order.getRestaurantId() == null || order.getItems() == null) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(order.getRestaurantId());
            if (optionalRestaurant.isEmpty()) {
                HttpError.notFound(exchange, "Restaurant not found");
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();
            int rawPrice = 0;
            for (OrderItem orderItem : order.getItems()) {
                Optional<Food> optionalFood = foodDAO.getById(orderItem.getItemId());
                if (optionalFood.isEmpty()) {
                    HttpError.notFound(exchange, "Food not found");
                    return;
                }
                Food food = optionalFood.get();
                if (!food.getRestaurantId().equals(order.getRestaurantId())) {
                    HttpError.unauthorized(exchange, "This food is not in the restaurant");
                    return;
                }
                rawPrice += food.getPrice();
            }

            Random rand = new Random();
            int randomPrice = rand.nextInt(91) + 10;

            order.setStatus(Status.unpaid);
            order.setRawPrice(rawPrice);
            order.setAdditionalFee(restaurant.getAdditionalFee());
            order.setTaxFee(restaurant.getTaxFee());
            order.setCourierFee(randomPrice);
            order.setPayPrice(rawPrice + restaurant.getAdditionalFee() + restaurant.getTaxFee() + randomPrice);
            order.setCustomerId(user.getId());
            order.setId(orderDAO.insert(order));
            order = orderDAO.getById(order.getId()).orElse(null);
            String jsonOutput = objectMapper.writeValueAsString(order);
            JsonResponse.sendJsonResponse(exchange, 200, jsonOutput);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleGetOrder(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can get orders");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int orderId = Integer.parseInt(parts[2]);

        try {
            Optional<Order> orderOpt = orderDAO.getById(orderId);
            if (orderOpt.isEmpty()) {
                HttpError.notFound(exchange, "Order not found");
                return;
            }
            Order order = orderOpt.get();
            if (!order.getCustomerId().equals(user.getId())) {
                HttpError.unauthorized(exchange, "You are not authorized to view this order");
                return;
            }

            String json = objectMapper.writeValueAsString(order);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    // TODO has query
    private void handleOrdersHistory(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can get orders history");
            return;
        }

        try {
            List<Order> orders = orderDAO.getUserOrders(user.getId());
            String json = objectMapper.writeValueAsString(orders);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleGetFavorites(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if  (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can get favorites");
            return;
        }

        try {
            List<Restaurant> favorites = userDAO.getFavorites(user.getId());
            String json = objectMapper.writeValueAsString(favorites);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleInsertFavorite(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can add favorites");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int restaurantId = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> restaurantOpt = restaurantDAO.getById(restaurantId);
            if (restaurantOpt.isEmpty()) {
                HttpError.notFound(exchange, "Restaurant not found");
                return;
            }
            userDAO.insertFavorite(user.getId(), restaurantOpt.get());
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Added to favorites\"}");
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleDeleteFavorite(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can delete favorites");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int restaurantID = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(restaurantID);
            if (optionalRestaurant.isEmpty()) {
                HttpError.notFound(exchange, "Restaurant not found");
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();
            if (!userDAO.getFavorites(user.getId()).contains(restaurant)) {
                HttpError.notFound(exchange, "Restaurant is not in the favorites");
                return;
            }

            userDAO.deleteFavorite(user.getId(), restaurant);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Removed from favorites\"}");
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handeSubmitRate(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        Rate rate = objectMapper.readValue(exchange.getRequestBody(), Rate.class);
        // رای ندادن نیز 0
        if (rate.getRating() == 0 || rate.getComment() == null) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
            return;
        }
        try {
            rate.setId(rateDAO.insert(rate));
            rate.setUserId(authenticatedUserOptional.get().getId());
            String json = objectMapper.writeValueAsString(rate);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handeGetFoodRates(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int foodId = Integer.parseInt(parts[3]);
        try {
            Optional<Food> optionalFood = foodDAO.getById(foodId);
            if (optionalFood.isEmpty()) {
                // ارور
                return;
            }
            Food food = optionalFood.get();
            List<Rate> rates = rateDAO.getFoodRates(food);
            String json = objectMapper.writeValueAsString(rates);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGetRate(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);
        try {
            Optional<Rate> optionalRate = rateDAO.getById(id);
            if (optionalRate.isEmpty()) {
                return;
            }
            Rate rate = optionalRate.get();
            String json = objectMapper.writeValueAsString(rate);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleDeleteRate(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);
        try {
            Optional<Rate> optionalRate = rateDAO.getById(id);
            if (optionalRate.isEmpty()) {
                return;
            }
            Rate rate = optionalRate.get();
            if (!rate.getUserId().equals(authenticatedUserOptional.get().getId())) {
                // صاحب اش نیست
                return;
            }
            rateDAO.deleteById(id);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Rate deleted\"}");
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleUpdateRate(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);
        try {
            Rate rate = objectMapper.readValue(exchange.getRequestBody(), Rate.class);
            rate.setId(id);
            Optional<Rate> optionalRate = rateDAO.getById(id);
            if (optionalRate.isEmpty()) {
                return;
            }
            Rate currRate = optionalRate.get();
            if (!currRate.getUserId().equals(authenticatedUserOptional.get().getId())) {
                // صاحب اش نیست
                return;
            }
            rateDAO.updateById(id, rate);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Rate updated\"}");
        }  catch (Exception e) {
            //            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }
}