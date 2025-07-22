package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import static org.FRFood.util.Authenticate.authenticate;
import static org.FRFood.util.Validation.validatePhoneNumber;

import java.util.*;
import java.io.IOException;
import java.sql.SQLException;

public class AuthHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final BankAccountDAO bankDAO;
    private final ObjectMapper objectMapper;

    public AuthHandler() {
        userDAO = new UserDAOImp();
        bankDAO = new BankAccountDAOImp();
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/auth/register" -> handleRegister(exchange);
                        case "/auth/login" -> handleLogin(exchange);
                        case "/auth/logout" -> handleLogout(exchange);
                        default -> HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "GET" -> {
                    if (path.equals("/auth/profile")) {
                        handleGetProfile(exchange);
                    } else {
                        HttpError.notFound(exchange, "Not Found");
                    }
                }
                case "PUT" -> {
                    if (path.equals("/auth/profile")) {
                        handleUpdateProfile(exchange);
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

    private void handleRegister(HttpExchange exchange) throws IOException {
        User user;
        try {
            user = objectMapper.readValue(exchange.getRequestBody(), User.class);
        } catch (Exception e) {
            HttpError.badRequest(exchange, "Invalid input");
            return;
        }

        if ((user.getBank() == null || user.getBank().getId() == null || user.getBank().getAccountNumber() == null) || user.getFullName() == null || user.getPhoneNumber() == null || user.getPassword() == null || user.getRole() == null) {
            HttpError.notFound(exchange, "Missing required fields");
            return;
        }

        if (!validatePhoneNumber(user.getPhoneNumber())) {
            HttpError.unsupported(exchange, "Invalid phone number");
            return;
        }

        try {
            if (userDAO.getByPhone(user.getPhoneNumber()).isPresent()) {
                HttpError.conflict(exchange, "Phone number already exists");
                return;
            }

            int userId = userDAO.insert(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully");
            responseBody.put("user_id", userId);
            responseBody.put("token", token);
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            JsonResponse.sendJsonResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            HttpError.internal(exchange, "Internal server error");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        User loginRequest;
        loginRequest = objectMapper.readValue(exchange.getRequestBody(), User.class);
        if (loginRequest.getPhoneNumber() == null || loginRequest.getPassword() == null) {
            HttpError.unauthorized(exchange, "Missing required fields");
            return;
        }

        try {
            Optional<User> OpUser = userDAO.getByPhone(loginRequest.getPhoneNumber());
            if (OpUser.isEmpty()) {
                HttpError.unauthorized(exchange, "Phone number not found");
                return;
            }
            User user = OpUser.get();
            if (!user.isConfirmed()) {
                HttpError.forbidden(exchange, "User not confirmed");
                return;
            }

            if (!user.getPassword().equals(loginRequest.getPassword())) {
                HttpError.unauthorized(exchange, "Invalid password");
                return;
            }

            String token = JwtUtil.generateToken(user);
            JsonResponse.sendJsonResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error");
        }
    }

    private void handleGetProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;

        User authenticatedUser = authenticatedUserOptional.get();
        String jsonResponse = objectMapper.writeValueAsString(authenticatedUser);
        JsonResponse.sendJsonResponse(exchange, 200, jsonResponse);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;

        JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"User logged out successfully\"}");
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) return;

        User currentUser = authenticatedUserOptional.get();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> updates = objectMapper.readValue(exchange.getRequestBody(), Map.class);
            boolean changed = false;

            if (updates.containsKey("full_name")) {
                currentUser.setFullName((String) updates.get("full_name"));
                changed = true;
            }
            if (updates.containsKey("phone_number")) {
                if (!validatePhoneNumber((String) updates.get("phone_number"))) {
                    HttpError.badRequest(exchange, "Invalid phone number");
                    return;
                }
                currentUser.setPhoneNumber((String) updates.get("phone_number"));
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
            if (updates.containsKey("profileImageBase64")) {
                currentUser.setPicture((String) updates.get("profileImageBase64"));
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
                if (changed && bankDAO.getById(currentBank.getId()).isPresent())
                    bankDAO.update(currentBank);
                else if (changed)
                    currentBank.setId(bankDAO.insert(currentBank));
            }

            if (changed) {
                userDAO.update(currentUser);
            }

            JsonResponse.sendJsonResponse(exchange, 200, "{\"message\":\"Profile updated successfully\"}");
        } catch (com.fasterxml.jackson.core.JsonProcessingException jsonEx) {
            HttpError.badRequest(exchange, "Invalid JSON input");
        } catch (SQLException e) {
            HttpError.internal(exchange, "Internal server error while updating profile");
        }
    }
}