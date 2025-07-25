package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;

import static org.FRFood.entity.Role.*;
import org.FRFood.util.BuyerReq.ItemsReq;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.util.BuyerReq.VendorsReq;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;

import java.time.LocalDate;
import java.util.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.naming.InsufficientResourcesException;

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
                    else if (path.matches("^/ratings/user/\\d+$")) doesHaveRate(exchange);
                    else if (path.matches("^/orders/\\d+$")) handleGetOrder(exchange);
                    else if (path.matches("^/ratings/\\d+$")) handleGetRate(exchange);
                    else if (path.equals("/coupons")) checkCoupon(exchange);
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
            if (req.keywords != null) {
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
            } else {
                restaurantsFiltered = restaurants;
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
                if ((req.keywords == null || foodDAO.doesHaveKeywords(req.keywords, food.getId())) && (req.minPrice <= food.getPrice() && food.getPrice() <= req.maxPrice)) {
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

    private void handleOrdersHistory(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can get orders history");
            return;
        }

        String query = exchange.getRequestURI().getQuery();

        try {
            List<Order> orders = orderDAO.getUserOrders(user.getId());
            List<Order> finalOrders = new ArrayList<>(orders);

            if (query != null && !query.isEmpty()) {
                String[] parts = query.split("&");
                Map<String, String> params = new HashMap<>();
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    params.put(keyValue[0], keyValue[1]);
                }
                for (Order order : orders) {
                    Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(order.getRestaurantId());
                    if (optionalRestaurant.isEmpty()) {
                        HttpError.notFound(exchange, "Restaurant not found");
                        return;
                    }
                    Restaurant restaurant = optionalRestaurant.get();
                    if (params.containsKey("vendor") && !restaurant.getName().contains(params.get("vendor"))) {
                        finalOrders.remove(order);
                    }
                    if (params.containsKey("search") && !params.get("search").isEmpty()) {
                        List<OrderItem> items = order.getItems();
                        boolean found = false;
                        for (OrderItem item : items) {
                            Optional<Food> optionalFood = foodDAO.getById(item.getItemId());
                            if (optionalFood.isEmpty()) {
                                HttpError.notFound(exchange, "Food not found");
                                return;
                            }
                            Food food = optionalFood.get();
                            if (food.getName().contains(params.get("search"))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            finalOrders.remove(order);
                        }
                    }
                }
            }

            String json = objectMapper.writeValueAsString(finalOrders);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error" + e.getMessage());
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
            HttpError.internal(exchange, "Internal server error"+e.getMessage());
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
            HttpError.internal(exchange, "Internal server error" + e.getMessage());
        }
    }

    private void handeSubmitRate(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can submit rate");
            return;
        }
        Rate rate = objectMapper.readValue(exchange.getRequestBody(), Rate.class);

        if (rate.getRating() == null || rate.getComment() == null || rate.getRating() < 0 || rate.getRating() > 5) {
            HttpError.badRequest(exchange, "Invalid rate");
            return;
        }
        try {
            rate.setUserId(user.getId());
            rate.setId(rateDAO.insert(rate));
            String json = objectMapper.writeValueAsString(rate);
            JsonResponse.sendJsonResponse(exchange, 200, json);

        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error" + e.getMessage());
        }
    }

    private void handeGetFoodRates(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int foodId = Integer.parseInt(parts[3]);

        try {
            Optional<Food> optionalFood = foodDAO.getById(foodId);
            if (optionalFood.isEmpty()) {
                HttpError.notFound(exchange, "Food not found");
                return;
            }

            Food food = optionalFood.get();
            List<Rate> rates = rateDAO.getAllRates();
            List<Rate> foodRates = new ArrayList<>();
            for (Rate rate : rates) {
                Optional<Order> optionalOrder = orderDAO.getById(rate.getOrderId());
                if (optionalOrder.isEmpty()) {
                    HttpError.notFound(exchange, "Order not found");
                    return;
                }
                Order order = optionalOrder.get();
                boolean found = false;
                for (OrderItem orderItem : order.getItems()) {
                    Optional<Food> optionalTempFood = foodDAO.getById(orderItem.getItemId());
                    if (optionalTempFood.isEmpty()) {
                        HttpError.notFound(exchange, "Food not found");
                        return;
                    }
                    Food tempFood = optionalTempFood.get();
                    if (tempFood.getId().equals(food.getId())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    foodRates.add(rate);
                }
            }

            int avg = 0;
            for (Rate rate : foodRates)
                avg += rate.getRating();
            if (!rates.isEmpty())
                avg /= rates.size();

            ObjectNode root = objectMapper.createObjectNode();
            JsonNode avgNode = objectMapper.valueToTree(avg);
            root.set("avg_rating", avgNode);
            JsonNode ratesNode = objectMapper.valueToTree(foodRates);
            root.set("comments", ratesNode);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleGetRate(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can get food rates");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Rate> optionalRate = rateDAO.getById(id);
            if (optionalRate.isEmpty()) {
                HttpError.notFound(exchange, "Rate not found");
                return;
            }

            Rate rate = optionalRate.get();
            String json = objectMapper.writeValueAsString(rate);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleDeleteRate(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can delete rates");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Rate> optionalRate = rateDAO.getById(id);
            if (optionalRate.isEmpty()) {
                HttpError.notFound(exchange, "Rate not found");
                return;
            }
            Rate rate = optionalRate.get();
            if (!rate.getUserId().equals(user.getId())) {
                HttpError.unauthorized(exchange, "You are not allowed to delete this rate");
                return;
            }

            rateDAO.deleteById(id);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Rate deleted\"}");
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleUpdateRate(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can update rates");
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
                HttpError.notFound(exchange, "Rate not found");
                return;
            }
            Rate currRate = optionalRate.get();
            if (!currRate.getUserId().equals(user.getId())) {
                HttpError.unauthorized(exchange, "You are not allowed to update this rate");
                return;
            }

            rateDAO.updateById(id, rate);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Rate updated\"}");
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void doesHaveRate(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can check how many rates");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int orderID = Integer.parseInt(parts[3]);

        try {
            List<Rate> rates = rateDAO.getUserRateOnOrder(user.getId(),  orderID);
            if (rates.isEmpty()) {
                JsonResponse.sendJsonResponse(exchange,469,"{\"message\":\"No\"}");
            } else {
                JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Yes\"}");
            }
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkCoupon(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can check coupons");
            return;
        }

        String couponCode;
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.isEmpty()) {
            String[] parts = query.split("&");
            Map<String, String> params = new HashMap<>();
            for (String part : parts) {
                String[] keyValue = part.split("=");
                params.put(keyValue[0], keyValue[1]);
            }
            if (!params.containsKey("coupon_code")) {
                HttpError.badRequest(exchange, "Coupon code not found");
                return;
            }
            couponCode = params.get("coupon_code");
        } else {
            HttpError.badRequest(exchange, "Coupon code not found");
            return;
        }
        try {
            CouponDAO couponDAO = new CouponDAOImp();
            Optional<Coupon> optionalCoupon = couponDAO.getByCode(couponCode);
            if (optionalCoupon.isEmpty()) {
                HttpError.notFound(exchange, "Coupon not found");
                return;
            }
            Coupon coupon = optionalCoupon.get();
            int userCount = couponDAO.getUserCount(coupon.getId(), user.getId());
            if (userCount >= coupon.getUserCount()) {
                HttpError.unauthorized(exchange, "You finish this coupon");
                return;
            }
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.parse(coupon.getStartDate());
            LocalDate end = LocalDate.parse(coupon.getEndDate());
            if (!((now.isEqual(start) || now.isAfter(start)) && (now.isEqual(end) || now.isBefore(end)))) {
                HttpError.unauthorized(exchange, "Coupon is not active");
                return;
            }

            String json =  objectMapper.writeValueAsString(coupon);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void topRestaurants(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can top restaurants");
            return;
        }

        try {
            Map<Integer, Integer> restaurantsScore = new HashMap<>();
            Map<Integer, Integer> restaurantsNumberOfScore = new HashMap<>();
            List<Rate> rates = rateDAO.getAllRates();

            for (Rate rate : rates) {
                Optional<Order> optionalOrder = orderDAO.getById(rate.getOrderId());
                if (optionalOrder.isEmpty()) {
                    HttpError.notFound(exchange, "Order not found");
                    return;
                }
                Order order = optionalOrder.get();
                int restaurantId = order.getRestaurantId();

                restaurantsScore.put(restaurantId, restaurantsScore.getOrDefault(restaurantId, 0) + rate.getRating());

                restaurantsNumberOfScore.put(restaurantId, restaurantsNumberOfScore.getOrDefault(restaurantId, 0) + 1);
            }

            List<Map.Entry<Integer, Double>> averageScores = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : restaurantsScore.entrySet()) {
                int restaurantId = entry.getKey();
                int totalScore = entry.getValue();
                int count = restaurantsNumberOfScore.getOrDefault(restaurantId, 1);
                double average = (double) totalScore / count;
                averageScores.add(Map.entry(restaurantId, average));
            }

            averageScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            List<Restaurant> topRestaurants = new ArrayList<>();
            int limit = Math.min(5, averageScores.size());

            for (int i = 0; i < limit; i++) {
                int restaurantId = averageScores.get(i).getKey();
                Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(restaurantId);
                if (optionalRestaurant.isEmpty()) {
                    HttpError.notFound(exchange, "Restaurant not found");
                    return;
                }
                Restaurant restaurant = optionalRestaurant.get();
                topRestaurants.add(restaurant);
            }

            String json = objectMapper.writeValueAsString(topRestaurants);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void topFoods(HttpExchange exchange) throws IOException {
        var userOpt = Authenticate.authenticate(exchange);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (!user.getRole().equals(buyer)) {
            HttpError.unauthorized(exchange, "Only buyers can top restaurants");
            return;
        }

        try {
            List<Order> orders = orderDAO.getUserOrders(user.getId());
            Map<Food, Integer> foodCount = new HashMap<>();

            for (Order order : orders) {
                for (OrderItem orderItem : order.getItems()) {
                    Optional<Food> foodOpt = foodDAO.getById(orderItem.getItemId());
                    if (foodOpt.isEmpty()) {
                        HttpError.notFound(exchange, "Food not found");
                        return;
                    }
                    Food food = foodOpt.get();
                    foodCount.put(food, foodCount.getOrDefault(food, 0) + orderItem.getQuantity());
                }
            }

            List<Food> topFoods = foodCount.entrySet()
                    .stream()
                    .sorted(Map.Entry.<Food, Integer>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            String json = objectMapper.writeValueAsString(topFoods);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }
}