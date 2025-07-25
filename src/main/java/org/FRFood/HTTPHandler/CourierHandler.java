package org.FRFood.HTTPHandler;

import java.util.*;
import java.io.IOException;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.FRFood.DAO.*;
import org.FRFood.entity.*;
import org.FRFood.util.HttpError;
import org.FRFood.util.JsonResponse;
import org.FRFood.entity.Status;

import static org.FRFood.entity.Role.courier;
import static org.FRFood.util.Authenticate.authenticate;

public class CourierHandler implements HttpHandler {
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;

    public CourierHandler() {
        orderDAO = new OrderDAOImp();
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String overrideMethod = exchange.getRequestHeaders().getFirst("X-HTTP-Method-Override");

        if ("POST".equalsIgnoreCase(method) && overrideMethod != null) {
            method = overrideMethod.toUpperCase();
        }

        try {
            switch (method) {
                case "GET" -> {
                    switch (path) {
                        case "/deliveries/available" -> handleGetOrders(exchange);
                        case "/deliveries/history" -> handleGetHistory(exchange);
                        case "/deliveries/order" -> handleActiveOrder(exchange);
                    }
                }
                case "PATCH" -> {
                    if (path.matches("^/deliveries/\\d+$")) {
                        handleChangeStatus(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                default -> HttpError.notFound(exchange, "Not Found");
            }
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal Server Error");
        }
    }

    private void handleGetOrders(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }

        try {
            List<Order> orders = orderDAO.getAvailableOrders();
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleChangeStatus(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int orderId = Integer.parseInt(parts[2]);
        JsonNode jsonNode = objectMapper.readTree(exchange.getRequestBody());
        if (!jsonNode.hasNonNull("status")) {
            HttpError.badRequest(exchange, "Missing required field: status");
            return;
        }
        String input = jsonNode.get("status").asText();
        Status status = Status.valueOf(input);
        if (status != Status.onTheWay && status != Status.completed) {
            HttpError.badRequest(exchange, "Invalid status");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.getById(orderId);
            if (optionalOrder.isEmpty()) {
                HttpError.notFound(exchange, "Not Found");
                return;
            }
            Order order = optionalOrder.get();
            if (!order.getCourierId().equals(0) && !order.getCourierId().equals(user.getId())) {
                HttpError.unauthorized(exchange, "This order has already been assigned");
                return;
            }

            if (status == Status.completed) {
                Transaction transaction = new Transaction();
                transaction.setOrderID(orderId);
                transaction.setUserID(user.getId());
                transaction.setAmount(order.getCourierFee());
                transaction.setMethod(TransactionMethod.online);
                new TransactionDAOImp().insert(transaction);
                new UserDAOImp().setWallet(user.getId(), user.getWallet() + order.getCourierFee());
            } else {
                int activeOrder = 0;
                List<Order> orders = orderDAO.getCourierOrders(user.getId());
                for (Order tempOrder : orders) {
                    if (tempOrder.getStatus() == Status.onTheWay) {
                        activeOrder++;
                    }
                }
                if (activeOrder >= 1) {
                    HttpError.forbidden(exchange, "There is a active order");
                    return;
                }
            }

            orderDAO.changeStatus(orderId, status, user.getId());
            JsonResponse.sendJsonResponse(exchange, 200, "{message: success}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        FoodDAO foodDAO = new FoodDAOImp();
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();

        try {
            List<Order> orders = orderDAO.getCourierOrders(user.getId());
            List<Order> finalOrders = new ArrayList<>(orders);
            UserDAO userDAO = new UserDAOImp();
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
                    if (params.containsKey("user") && !params.get("user").isEmpty()) {
                        Optional<User> optionalUser = userDAO.getById(order.getCustomerId());
                        if (optionalUser.isPresent() && !optionalUser.get().getFullName().contains(params.get("user")))
                            finalOrders.remove(order);
                    }
                    if (params.containsKey("search")) {
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

            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(finalOrders));
        }  catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleActiveOrder(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }

        try {
            List<Order> orders = orderDAO.getCourierOrders(user.getId());
            List<Order> activeOrder =  new ArrayList<>();
            for (Order order : orders) {
                if (order.getStatus() == Status.onTheWay) {
                    activeOrder.add(order);
                }
            }
            if (activeOrder.size() > 1) {
                HttpError.badRequest(exchange, "There are more than one active order");
                return;
            }
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(activeOrder));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}