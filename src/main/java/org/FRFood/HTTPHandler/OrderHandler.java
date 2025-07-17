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

public class OrderHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;
    private final TransactionDAO transactionDAO;

    public OrderHandler() {
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
                    if (path.equals("/transactions")) {
                        handleGetTransactions(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "POST" -> {
                    if (path.equals("/wallet/top-up")) {
                        handleWallet(exchange);
                    } else if (path.equals("/payment/online")) {
                        handlePay(exchange);
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

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();

        try {
            List<Transaction> transactions = transactionDAO.getUserTransactions(user.getId());
            JsonResponse.sendJsonResponse(exchange, 200, objectMapper.writeValueAsString(transactions));
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handleWallet(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();

        int amount = objectMapper.readTree(exchange.getRequestBody()).get("amount").asInt();
        if (amount == 0) {
            HttpError.notFound(exchange, "Status not found");
            return;
        }

        try {
            // TODO need to check
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setUserID(user.getId());
            transaction.setId(transactionDAO.insert(transaction));
            userDAO.setWallet(user.getId(), user.getWallet()+amount);
            JsonResponse.sendJsonResponse(exchange, 200, "{message: wallet updated}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }

    private void handlePay(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        Transaction transaction = objectMapper.readValue(exchange.getRequestBody(), Transaction.class);

        try {
            // TODO need to check
            Optional<Order> orderOptional = orderDAO.getById(transaction.getOrderID());
            if (orderOptional.isEmpty()) {
                HttpError.notFound(exchange, "Status not found");
                return;
            }
            Order order = orderOptional.get();
            transaction.setUserID(user.getId());
            transaction.setAmount(order.getPayPrice());
            transaction.setId(transactionDAO.insert(transaction));
            JsonResponse.sendJsonResponse(exchange, 200, "{message: payed}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}