package org.FRFood.HTTPHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.FRFood.entity.Order;
import org.FRFood.entity.User;
import org.FRFood.DAO.OrderDAO;
import org.FRFood.DAO.OrderDAOImp;
import org.FRFood.util.HttpError;
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
                        case "/deliveries/history" -> handleGetHistoy(exchange);
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
        }  catch (SQLException e) {
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
        //TODO
    }

    private void handleGetHistoy(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        if (!user.getRole().equals(courier)) {
            HttpError.forbidden(exchange, "Only for Courier");
            return;
        }
        //TODO
    }
}
