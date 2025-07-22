package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.DAO.*;
import org.FRFood.entity.*;
import org.FRFood.util.BuyerReq.OrderReq;
import org.FRFood.util.HttpError;
import org.FRFood.util.JsonResponse;
import org.FRFood.util.Status;
import org.FRFood.util.TransactionMethod;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.FRFood.util.Authenticate.authenticate;
import static org.FRFood.util.TransactionMethod.wallet;

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
            HttpError.notFound(exchange, "Amount is zero");
            return;
        }
        try {
            Transaction transaction = new Transaction();
            transaction.setOrderID(0);
            transaction.setUserID(user.getId());
            transaction.setMethod(
                    TransactionMethod.online
            );
            transaction.setAmount(amount);

            transaction.setId(transactionDAO.insert(transaction));
            userDAO.setWallet(user.getId(), user.getWallet()+amount);
            JsonResponse.sendJsonResponse(exchange, 200, "{message: wallet updated}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile" + e.getMessage());
        }
    }

    private void handlePay(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;
        User user = authenticatedUserOptional.get();
        Transaction transaction = new Transaction();
        OrderReq orderReq = objectMapper.readValue(exchange.getRequestBody(), OrderReq.class);
        Order order = orderReq.order;
        transaction.setOrderID(order.getId());
        transaction.setMethod(orderReq.method);

        if (order.getDeliveryAddress() == null || order.getRestaurantId() == null || order.getItems() == null) {
            HttpError.badRequest(exchange, "Missing required fields");
            return;
        }

        FoodDAO foodDAO = new FoodDAOImp();
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(order.getRestaurantId());
            if (optionalRestaurant.isEmpty()) {
                HttpError.notFound(exchange, "Restaurant not found");
                return;
            }
//            Restaurant restaurant = optionalRestaurant.get();
//            int rawPrice = 0;
//            for (OrderItem orderItem : order.getItems()) {
//                Optional<Food> optionalFood = foodDAO.getById(orderItem.getItemId());
//                if (optionalFood.isEmpty()) {
//                    HttpError.notFound(exchange, "Food not found");
//                    return;
//                }
//                Food food = optionalFood.get();
//                if (!food.getRestaurantId().equals(order.getRestaurantId())) {
//                    HttpError.unauthorized(exchange, "This food is not in the restaurant");
//                    return;
//                }
//                if (food.getSupply() < orderItem.getQuantity()) {
//                    HttpError.badRequest(exchange, "Supply is less than to quantity");
//                    return;
//                }
//                rawPrice += food.getPrice()*orderItem.getQuantity();
//            }
//
            for (OrderItem orderItem : order.getItems()) {
                Optional<Food> optionalFood = foodDAO.getById(orderItem.getItemId());
                if (optionalFood.isEmpty()) return;
                Food food = optionalFood.get();
                food.setSupply(food.getSupply()-orderItem.getQuantity());
                foodDAO.update(food);
            }
//
//            Random rand = new Random();
//            int randomPrice = rand.nextInt(91) + 10;
//
            order.setStatus(Status.waiting);
//            order.setRawPrice(rawPrice);
//            order.setAdditionalFee(restaurant.getAdditionalFee());
//            order.setTaxFee(restaurant.getTaxFee());
//            order.setCourierFee(randomPrice);
//            order.setCouponId(0);
//            order.setCourierId(0);
//            order.setPayPrice(rawPrice + restaurant.getAdditionalFee() + restaurant.getTaxFee() + randomPrice);
//            order.setCustomerId(user.getId());

            transaction.setUserID(user.getId());
            transaction.setAmount(order.getPayPrice());

            if (transaction.getMethod().equals(wallet)) {
                if (user.getWallet() < transaction.getAmount()) {
                    HttpError.unauthorized(exchange, "Not enough money");
                    return;
                } else {
                    user.setWallet(user.getWallet() - transaction.getAmount());
                }
            }
            order.setId(orderDAO.insert(order));
            transaction.setOrderID(order.getId());
            transaction.setId(transactionDAO.insert(transaction));
            JsonResponse.sendJsonResponse(exchange, 200, "{message: payed}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}