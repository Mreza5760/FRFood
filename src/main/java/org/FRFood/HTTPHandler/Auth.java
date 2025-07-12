package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.io.IOException;

public class Auth implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final UserDAO userDAO;
    private final BankAccountDAO bankDAO;

    public Auth() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.userDAO = new UserDAOImp();
        this.bankDAO = new BankAccountDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/auth/register" -> handleRegister(exchange);
                        case "/auth/login" -> handleLogin(exchange);
                        case "/auth/logout" -> handleLogout(exchange);
                        default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
                    }
                }
                case "GET" -> {
                    if (path.equals("/auth/profile")) {
                        handleGetProfile(exchange);
                    } else {
                        JsonResponse.sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                    }
                }
                case "PUT" -> {
                    if (path.equals("/auth/profile")) {
                        handleUpdateProfile(exchange);
                    } else {
                        JsonResponse.sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                    }
                }
                default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        User user;
        try {
            user = objectMapper.readValue(exchange.getRequestBody(), User.class);
        } catch (Exception e) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Invalid input\"}");
            return;
        }

        if (user.getFullName() == null || user.getPassword() == null ||
                user.getRole() == null || user.getBank() == null ||
                user.getBank().getName() == null || user.getBank().getAccountNumber() == null) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
            return;
        }

        try {
            if (userDAO.getByPhone(user.getPhoneNumber()).isPresent()) {
                JsonResponse.sendJsonResponse(exchange, 409, "{\"error\":\"Phone number already exists\"}");
                return;
            }

            int bankId = bankDAO.insert(user.getBank());
            user.getBank().setId(bankId);
            int userId = userDAO.insert(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully");
            responseBody.put("user_id", userId);
            responseBody.put("token", token);
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            JsonResponse.sendJsonResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        User loginRequest = objectMapper.readValue(exchange.getRequestBody(), User.class);

        try {
            User user;
            Optional<User> OpUser = userDAO.getByPhone(loginRequest.getPhoneNumber());
            if (OpUser.isPresent()){
                user = OpUser.get();
                if (!user.getPassword().equals(loginRequest.getPassword())){
                    JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Invalid password\"}");
                    return;
                }
            } else {
                JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"phone number not found\"}");
                return;
            }

            String token = JwtUtil.generateToken(user);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    private void handleGetProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        User authenticatedUser = authenticatedUserOptional.get();
        String jsonResponse = this.objectMapper.writeValueAsString(authenticatedUser);
        JsonResponse.sendJsonResponse(exchange, 200, jsonResponse);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"User logged out successfully\"}");
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        User currentUser = authenticatedUserOptional.get();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> updates = this.objectMapper.readValue(exchange.getRequestBody(), Map.class);
            boolean changed = false;
            if (updates.containsKey("full_name")) {
                currentUser.setFullName((String) updates.get("full_name"));
                changed = true;
            }
            if (updates.containsKey("email")) {
                currentUser.setEmail((String) updates.get("email"));
                changed = true;
            }
            if (updates.containsKey("password")) {
                currentUser.setPassword((String) updates.get("password_hash"));
                changed = true;
            }
            if (updates.containsKey("address")) {
                currentUser.setAddress((String) updates.get("address"));
                changed = true;
            }
            if (updates.containsKey("profile_image")) {
                currentUser.setPicture((String) updates.get("profile_image"));
                changed = true;
            }
            if (updates.containsKey("bank_info")) {
                @SuppressWarnings("unchecked")
                Map<String, String> bankInfoUpdates = (Map<String, String>) updates.get("bank_info");
                BankAccount currentBank = currentUser.getBank();
                if (bankInfoUpdates.containsKey("bank_name")) {
                    currentBank.setName(bankInfoUpdates.get("bank_name"));
                    changed = true;
                }
                if (bankInfoUpdates.containsKey("account_number")) {
                    currentBank.setAccountNumber(bankInfoUpdates.get("account_number"));
                    changed = true;
                }
                if (changed && bankDAO.getById(currentBank.getId()).isPresent()) {
                    this.bankDAO.update(currentBank);
                } else if (changed && bankDAO.getById(currentBank.getId()).isEmpty()) {
                    int bankId = this.bankDAO.insert(currentBank);
                    currentBank.setId(bankId);
                }
            }
            if (changed) {
                this.userDAO.update(currentUser);
            }
            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Profile updated successfully\"}");
        } catch (com.fasterxml.jackson.core.JsonProcessingException jsonEx) {
            JsonResponse.sendJsonResponse(exchange, 400, "{\"error\":\"Invalid JSON input\"}");
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error while updating profile\"}");
        }
    }
}