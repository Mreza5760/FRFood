package org.FRFood.HTTPHandler;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.sql.SQLException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.FRFood.entity.User;
import org.FRFood.DAO.OrderDAO;
import org.FRFood.entity.Order;
import org.FRFood.util.HttpError;
import org.FRFood.DAO.OrderDAOImp;
import org.FRFood.util.JsonResponse;
import static org.FRFood.util.Role.courier;
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
        try {
            switch (method) {
                case "GET" -> {
                    switch (path) {
                        case "/deliveries/available" -> handleGetOrders(exchange);
                        case "/deliveries/history" -> handleGetHistory(exchange);
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
        String status = objectMapper.readTree(exchange.getRequestBody()).get("status").asText();
        // TODO need to check
        if (status.equals("0")) {
            HttpError.notFound(exchange, "Status not found");
            return;
        }

        try {
            Optional<Order> optionalOrder = orderDAO.getById(orderId);
            if (optionalOrder.isEmpty()) {
                HttpError.notFound(exchange, "Not Found");
                return;
            }
            Order order = optionalOrder.get();
            if (order.getCourierId() != 0 && order.getCourierId() != user.getId()) {
                HttpError.unauthorized(exchange, "This order has already been assigned");
                return;
            }

            orderDAO.changeStatus(orderId, status);
            JsonResponse.sendJsonResponse(exchange, 200, "{message: success}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    // TODO has query
    private void handleGetHistory(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }

        try {
            List<Order> orders = orderDAO.getCourierOrders(user.getId());
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        }  catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}