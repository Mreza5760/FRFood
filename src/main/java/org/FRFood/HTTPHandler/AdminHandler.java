package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.DAO.*;
import org.FRFood.entity.Order;
import org.FRFood.entity.Transaction;
import org.FRFood.entity.User;
import org.FRFood.util.HttpError;
import org.FRFood.util.JsonResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
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
                        approveUser(exchange);
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

        if (user.getRole().equals(admin)) {
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

    private void approveUser(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();

        if (user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[3]);

        try {
            Optional<User> optionalUser = userDAO.getById(id);
            if (optionalUser.isEmpty()) {
                HttpError.notFound(exchange, "Not Found");
                return;
            }
            User userToUpdate = optionalUser.get();
            if (userToUpdate.isConfirmed()) {
                HttpError.badRequest(exchange, "User is already confirmed");
                return;
            }

            userDAO.setConfirmed(userToUpdate.getId());
            JsonResponse.sendJsonResponse(exchange, 200, "{message: success}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    // TODO has query
    private void handleGetOrders(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();

        if (user.getRole().equals(admin)) {
            HttpError.forbidden(exchange, "Only Admin Allowed");
            return;
        }

        try {
            List<Order> orders = orderDAO.getAllOrders();
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(orders));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        Optional<User> optionalUser = authenticate(exchange);
        if (optionalUser.isEmpty()) return;
        User user = optionalUser.get();
        if (user.getRole().equals(admin)) {
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