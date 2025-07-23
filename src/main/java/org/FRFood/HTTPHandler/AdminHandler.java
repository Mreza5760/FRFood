package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.DAO.*;
import org.FRFood.entity.*;
import org.FRFood.util.HttpError;
import org.FRFood.util.JsonResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.FRFood.util.Authenticate.authenticate;
import static org.FRFood.util.Role.admin;

public class AdminHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;
    private final TransactionDAO transactionDAO;

    public AdminHandler() {
        userDAO = new UserDAOImp();
        orderDAO = new OrderDAOImp();
        objectMapper = new ObjectMapper();
        transactionDAO = new TransactionDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (method) {
                case "GET" -> {
                    switch (path) {
                        case "/admin/users" -> handleGetUsers(exchange);
                        case "/admin/orders" -> handleGetOrders(exchange);
                        case "/admin/transactions" -> handleGetTransactions(exchange);
                        default -> HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "PATH" -> {
                    if (path.matches("/admin/users/\\d+/status")) {
                        handleUserStatus(exchange);
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

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();

        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        try {
            List<User> users = userDAO.getAllUser();
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(users));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleUserStatus(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();

        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[3]);

        JsonNode jsonNode = objectMapper.readTree(exchange.getRequestBody());
        String status = jsonNode.path("status").asText();

        try {
            Optional<User> optionalUser = userDAO.getById(id);
            if (optionalUser.isEmpty()) {
                HttpError.notFound(exchange, "User not found");
                return;
            }
            User targetUser = optionalUser.get();

            switch (status) {
                case "approved" -> {
                    if (targetUser.isConfirmed()) {
                        HttpError.badRequest(exchange, "User is already confirmed");
                        return;
                    }
                    userDAO.makeConfirmed(id);
                    JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"User approved\"}");
                }
                case "declined" -> {
                    userDAO.deleteById(id);
                    JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"User declined and deleted\"}");
                }
                default -> HttpError.badRequest(exchange, "Invalid status");
            }

        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating user");
        }
    }

    private void handleGetOrders(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        FoodDAO foodDAO = new FoodDAOImp();
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();

        try {
            List<Order> orders = orderDAO.getAllOrders();

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
                        orders.remove(order);
                    } else if (params.containsKey("customer") && order.getCustomerId() != Integer.parseInt(params.get("customer"))) {
                        orders.remove(order);
                    } else if (params.containsKey("courier") && order.getCourierId() != Integer.parseInt(params.get("courier"))) {
                        orders.remove(order);
                    } else if (params.containsKey("status") && !order.getStatus().toString().equals(params.get("status"))) {
                        orders.remove(order);
                    } else if (params.containsKey("search")) {
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
                            orders.remove(order);
                        }
                    }
                }
            }

            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    // TODO has query
    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        try {
            List<Transaction> transactions = transactionDAO.getAllTransactions();
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(transactions));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}