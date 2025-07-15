package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.DTO.OrderInputDTO;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import org.FRFood.util.BuyerReq.ItemsReq;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.util.BuyerReq.VendorsReq;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;

import java.util.List;
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
        // سوییچ کیس های داخلی هم نیازمند حالت دیفالت هستند
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/vendors" -> handleVendorsList(exchange);
                        case "/items" -> handleItemsList(exchange);
                        case "/orders" -> handleSubmitOrder(exchange);
                        case "/ratings" -> handeSubmitRate(exchange);
                    }
                }
                case "GET" -> {
                    switch (path) {
                        case "^/vendors/[^/]+$" -> handleVendorsMenu(exchange);
                        case "^/items/[^/]+$" -> handleGetItem(exchange);
                        case  "/orders/history" -> handleOrdersHistory(exchange);
                        case "/favorites" -> handleGetFavorites(exchange);
                        case "^/ratings/items/[^/]+$" -> handeGetFoodRates(exchange);
                        default -> {
                            if (path.equals("^/orders/[^/]+$")) {
                                handleGetOrder(exchange);
                            } else if (path.equals("^/ratings/[^/]+$")) {
                                handleGetRate(exchange);
                            }
                        }
                    }
                }
                case "PUT" -> {
                    switch (path) {
                        case "^/favorites/[^/]+$" -> handleInsertFavorite(exchange);
                        case "^/ratings/[^/]+$" -> handleUpdateRate(exchange);
                    }
                }
                case "DELETE" -> {
                    switch (path) {
                        case "^/favorites/[^/]+$" -> handleDeleteFavorite(exchange);
                        case "^/ratings/[^/]+$" -> handleDeleteRate(exchange);
                    }
                }
                default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleVendorsList(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        VendorsReq req = objectMapper.readValue(exchange.getRequestBody(), VendorsReq.class);

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
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void handleVendorsMenu(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(id);
            if (optionalRestaurant.isEmpty()) {
                // باید ارور وجود نداشتن رستوران داد
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
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void handleItemsList(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        ItemsReq req = objectMapper.readValue(exchange.getRequestBody(), ItemsReq.class);

        try {
            List<Food> foodsFiltered = new ArrayList<>();
            List<Restaurant> restaurants = restaurantDAO.searchByString(req.search);
            for (Restaurant restaurant : restaurants) {
                List<Food> foods = restaurantDAO.getFoods(restaurant.getId());
                for (Food food : foods) {
                    // تخفیف میشه هم لحاظ بشه هم نه
                    if (foodDAO.doesHaveKeywords(req.keywords , food.getId()) && food.getPrice() <= req.price) {
                        foodsFiltered.add(food);
                    }
                }
            }
            String json = objectMapper.writeValueAsString(foodsFiltered);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void  handleGetItem(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Food> optionalFood = foodDAO.getById(id);
            if (optionalFood.isEmpty()) {
                // باید ارور وجود نداشتن رستوران داد
                return;
            }
            Food food = optionalFood.get();
            String json = objectMapper.writeValueAsString(food);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void handleSubmitOrder(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        OrderInputDTO orderDTO = objectMapper.readValue(exchange.getRequestBody(), OrderInputDTO.class);
        // ایدی صفر نمیشه داشت
        if (orderDTO.getDeliveryAddress() == null || orderDTO.getRestaurantId() == 0 || orderDTO.getItems() == null) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
            return;
        }
        Order order = new Order();
        order.setCustomerId(authenticatedUserOptional.get().getId());
        try {
            order.setId(orderDAO.insert(orderDTO));
            order = orderDAO.getById(order.getId()).orElse(null);
            String jsonOutput = objectMapper.writeValueAsString(order);
            JsonResponse.sendJsonResponse(exchange, 200, jsonOutput);
            /*
                اوردر رو باید خروجی داد
             */
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handleGetOrder(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Order> optionalOrder = orderDAO.getById(id);
            if (optionalOrder.isEmpty()) {
                return;
            }
            Order order = optionalOrder.get();

            // ارور بده صاحب اش نبود
            if (order.getCustomerId() !=  authenticatedUserOptional.get().getId()) {
                return;
            }
            String  json = objectMapper.writeValueAsString(order);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handleOrdersHistory(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        try {
            int customerId = authenticatedUserOptional.get().getId();
            List<Order> orders = orderDAO.getUserOrders(customerId);
            String json = objectMapper.writeValueAsString(orders);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handleGetFavorites(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        try {
            int customerId = authenticatedUserOptional.get().getId();
            List<Restaurant> restaurants = userDAO.getFavorites(customerId);
            String json = objectMapper.writeValueAsString(restaurants);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handleInsertFavorite(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int restaurantID = Integer.parseInt(parts[2]);

        try {
            int customerId = authenticatedUserOptional.get().getId();
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(restaurantID);
            if (optionalRestaurant.isEmpty()) {
                // ارور
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();
            userDAO.insertFavorite(customerId, restaurant);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Added to favorites\"}");
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handleDeleteFavorite(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int restaurantID = Integer.parseInt(parts[2]);

        try {
            int customerId = authenticatedUserOptional.get().getId();
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(restaurantID);
            if (optionalRestaurant.isEmpty()) {
                // ارور
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();
            userDAO.deleteFavorite(customerId, restaurant);
            JsonResponse.sendJsonResponse(exchange,200,"{\"message\":\"Removed from favorites\"}");
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    void handeSubmitRate(HttpExchange exchange) throws IOException {
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

    void handeGetFoodRates(HttpExchange exchange) throws IOException {
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

    void handleGetRate(HttpExchange exchange) throws IOException {
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

    void handleDeleteRate(HttpExchange exchange) throws IOException {
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
            if (rate.getUserId() != authenticatedUserOptional.get().getId()) {
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

    void handleUpdateRate(HttpExchange exchange) throws IOException {
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
            if (currRate.getUserId() != authenticatedUserOptional.get().getId()) {
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