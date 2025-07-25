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
import java.util.*;

import static org.FRFood.util.Authenticate.authenticate;
import static org.FRFood.entity.Role.admin;

public class AdminHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final CouponDAO  couponDAO;
    private final ObjectMapper objectMapper;
    private final TransactionDAO transactionDAO;

    public AdminHandler() {
        userDAO = new UserDAOImp();
        orderDAO = new OrderDAOImp();
        couponDAO = new CouponDAOImp();
        objectMapper = new ObjectMapper();
        transactionDAO = new TransactionDAOImp();
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
                case "POST" -> {
                    if (path.equals("/admin/coupons")) {
                        handleCreateCoupon(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "GET" -> {
                    if (path.equals("/admin/users")) {
                        handleGetUsers(exchange);
                    } else if (path.equals("/admin/orders")) {
                        handleGetOrders(exchange);
                    } else if (path.equals("/admin/transactions")) {
                        handleGetTransactions(exchange);
                    } else if (path.matches("^/admin/coupons/\\d+$")) {
                        handleGetCoupon(exchange);
                    } else if (path.equals("/admin/coupons")) {
                        handleGetCoupons(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "PATCH" -> {
                    if (path.matches("/admin/users/\\d+/status")) {
                        handleUserStatus(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "PUT" -> {
                    if ((path.matches("^/admin/coupons/\\d+$"))) {
                        handleUpdateCoupon(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "DELETE" -> {
                    if ((path.matches("^/admin/coupons/\\d+$"))) {
                        handleDeleteCoupon(exchange);
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
        if (user.getRole() != admin) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        FoodDAO foodDAO = new FoodDAOImp();
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();

        try {
            List<Order> orders = orderDAO.getAllOrders();
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
                    if (params.containsKey("customer") && !params.get("customer").isEmpty()) {
                        Optional<User> optionalUser2 = userDAO.getById(order.getCustomerId());
                        if (optionalUser2.isPresent() && !optionalUser2.get().getFullName().contains(params.get("customer")))
                            finalOrders.remove(order);
                    }
                    if (params.containsKey("courier") && !params.get("courier").isEmpty()) {
                        Optional<User> optionalUser2 = userDAO.getById(order.getCourierId());
                        if (order.getCourierId() == 0 || (optionalUser2.isPresent() && !optionalUser2.get().getFullName().contains(params.get("courier"))))
                            finalOrders.remove(order);
                    }
                    if (params.containsKey("status") && !params.get("status").equals("null") && !params.get("status").isEmpty() && !order.getStatus().toString().equals(params.get("status"))) {
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

    private void handleCreateCoupon(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }
        Coupon coupon = objectMapper.readValue(exchange.getRequestBody(), Coupon.class);
        try {
            if (coupon.getCouponCode() == null || coupon.getType() == null || coupon.getValue() == null || coupon.getUserCount() == null || coupon.getMinPrice() == null) {
                HttpError.notFound(exchange, "Required field not found");
                return;
            }
            if (couponDAO.getByCode(coupon.getCouponCode()).isPresent()) {
                HttpError.forbidden(exchange, "Coupon already exists");
                return;
            }
            coupon.setId(couponDAO.insert(coupon));
            String json = objectMapper.writeValueAsString(coupon);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while creating coupon");
        }
    }

    private void handleGetCoupons(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        try {
            List<Coupon> coupons = couponDAO.getAllCoupons();
            String json = objectMapper.writeValueAsString(coupons);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while getting coupons");
        }
    }

    private void handleDeleteCoupon(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[3]);

        try {
            Optional<Coupon> optionalCoupon = couponDAO.getById(id);
            if (optionalCoupon.isEmpty()) {
                HttpError.notFound(exchange, "Coupon not found");
                return;
            }
            Coupon coupon = optionalCoupon.get();
            couponDAO.delete(coupon.getId());
            JsonResponse.sendJsonResponse(exchange, 200, "Coupon deleted");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while deleting coupon");
        }
    }

    private void handleUpdateCoupon(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[3]);

        try {
            Optional<Coupon> optionalCoupon = couponDAO.getById(id);
            if (optionalCoupon.isEmpty()) {
                HttpError.notFound(exchange, "Coupon not found");
                return;
            }
            Coupon curCoupon = optionalCoupon.get();
            Coupon coupon = objectMapper.readValue(exchange.getRequestBody(), Coupon.class);
            if (coupon.getMinPrice() != null) {
                curCoupon.setMinPrice(coupon.getMinPrice());
            }
            if (coupon.getCouponCode() != null) {
                curCoupon.setCouponCode(coupon.getCouponCode());
            }
            if (coupon.getType() != null) {
                curCoupon.setType(coupon.getType());
            }
            if (coupon.getValue() != null) {
                curCoupon.setValue(coupon.getValue());
            }
            if (coupon.getUserCount() != null) {
                curCoupon.setUserCount(coupon.getUserCount());
            }
            if (coupon.getStartDate() != null) {
                curCoupon.setStartDate(coupon.getStartDate());
            }
            if (coupon.getEndDate() != null) {
                curCoupon.setEndDate(coupon.getEndDate());
            }

            couponDAO.update(id, curCoupon);
            String json = objectMapper.writeValueAsString(coupon);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating coupon");
        }
    }

    private void handleGetCoupon(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (!user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[3]);

        try {
            Optional<Coupon> optionalCoupon = couponDAO.getById(id);
            if (optionalCoupon.isEmpty()) {
                HttpError.notFound(exchange, "Coupon not found");
                return;
            }
            Coupon coupon = optionalCoupon.get();
            String json = objectMapper.writeValueAsString(coupon);
            JsonResponse.sendJsonResponse(exchange, 200, json);
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while getting coupon");
        }
    }
}