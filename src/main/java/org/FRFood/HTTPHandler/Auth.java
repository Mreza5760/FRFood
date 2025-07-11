package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import io.jsonwebtoken.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.security.SignatureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
            if (method.equals("POST")) {
                if (path.equals("/auth/register")) {
                    handleRegister(exchange);
                } else if (path.equals("/auth/login")) {
                    handleLogin(exchange);
                } else if (path.equals("/auth/logout")) {
                    handleLogout(exchange);
                }else {
                    sendJsonResponse(exchange, 404, "Not Found");
                }
            } else if (method.equals("GET")) {
                if (path.equals("/auth/profile")) {
                    handleGetProfile(exchange); // New
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                }
            } else if (method.equals("PUT")) {
                if (path.equals("/auth/profile")) {
                    handleUpdateProfile(exchange); // New
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                }
            } else {
                sendJsonResponse(exchange, 404, "Not Found");
            }
        }catch (Exception e){
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        // Step 1: Parse request body
        User user;
        try {
            user = objectMapper.readValue(exchange.getRequestBody(), User.class);
        } catch (Exception e) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid input\"}");
            return;
        }

        // Step 2: Check required fields manually (if needed)
        if (user.getFullName() == null || user.getPassword() == null ||
                user.getRole() == null || user.getBank() == null ||
                user.getBank().getName() == null || user.getBank().getAccountNumber() == null) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
            return;
        }

        try {
            // Step 3: Check if user with same phone already exists
            if (userDAO.getByPhone(user.getPhoneNumber()).isPresent()) {
                sendJsonResponse(exchange, 409, "{\"error\":\"Phone number already exists\"}");
                return;
            }

            // Step 4: Insert bank first, then set the bank ID on the user
            int bankId = bankDAO.insert(user.getBank());
            user.getBank().setId(bankId);

            // Step 5: Insert the user
            int userId = userDAO.insert(user);

            // Step 6: Generate token (mocked here, use real JWT in production)
            String token = JwtUtil.generateToken(user); // or use UUID as placeholder if JWT is not ready

            // Step 7: Send success response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully");
            responseBody.put("user_id", userId);
            responseBody.put("token", token);

            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            sendJsonResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        User loginRequest = objectMapper.readValue(exchange.getRequestBody(), User.class);

        try {
            User user = null;
            Optional<User> OpUser = userDAO.getByPhone(loginRequest.getPhoneNumber());
            if(OpUser.isPresent()){
                user = OpUser.get();
                if(!user.getPassword().equals(loginRequest.getPassword())){
                    sendJsonResponse(exchange, 401, "{\"error\":\"Invalid password\"}");
                    return;
                }
            }else{
                sendJsonResponse(exchange, 401, "{\"error\":\"phone number not found\"}");
                return;
            }

            String token = JwtUtil.generateToken(user);
            sendJsonResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "Database error");
        }
    }

    private void handleGetProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        User authenticatedUser = authenticatedUserOptional.get();
        String jsonResponse = this.objectMapper.writeValueAsString(authenticatedUser);
        sendJsonResponse(exchange, 200, jsonResponse);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        sendJsonResponse(exchange, 200, "{\"message\":\"User logged out successfully\"}");
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }
        User currentUser = authenticatedUserOptional.get();

        try{
            @SuppressWarnings("unchecked")
            Map<String, Object> updates = this.objectMapper.readValue(exchange.getRequestBody(), Map.class);
            boolean changed = false;
            if(updates.containsKey("full_name")){
                currentUser.setFullName((String) updates.get("full_name"));
                changed = true;
            }
            if(updates.containsKey("email")){
                currentUser.setEmail((String) updates.get("email"));
                changed = true;
            }
            if(updates.containsKey("password")){
                currentUser.setPassword((String) updates.get("password_hash"));
                changed = true;
            }
            if(updates.containsKey("address")){
                currentUser.setAddress((String) updates.get("address"));
                changed = true;
            }
            if(updates.containsKey("profile_image")){
                currentUser.setPicture((String) updates.get("profile_image"));
                changed = true;
            }
            if(updates.containsKey("bank_info")){
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
                    //already has a bank account
                    this.bankDAO.update(currentBank);
                } else if (changed && bankDAO.getById(currentBank.getId()).isEmpty()) {
                    //needs a new bank account
                    int bankId = this.bankDAO.insert(currentBank);
                    currentBank.setId(bankId);
                }
            }
            if (changed) {
                this.userDAO.update(currentUser); // You'll need an update method in UserDAO
            }
            sendJsonResponse(exchange, 200, "{\"message\":\"Profile updated successfully\"}");
        }catch (com.fasterxml.jackson.core.JsonProcessingException jsonEx) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid JSON input\"}");
        } catch (Exception e) { // Catch other exceptions like DB errors
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error while updating profile\"}");
        }
    }

    private Optional<User> authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Missing or malformed Bearer token\"}");
            return Optional.empty();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // 1. Validate the token and get the claims object
            Jws<Claims> claimsJws = JwtUtil.validateToken(token); // Your method that throws exceptions
            Claims claims = claimsJws.getBody();

            // 2. Extract User ID from claims
            int userId = Integer.parseInt(claims.getSubject());
            // String userRoleFromToken = claims.get("role", String.class); // You can also get other claims

            // 3. Fetch the user from the database
            Optional<User> userOptional = this.userDAO.getById(userId); // You'll need a getById(int id) in UserDAO

            if (!userOptional.isPresent()) {
                sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: User associated with token not found\"}");
                return Optional.empty();
            }

            return userOptional;
        }catch (SQLException e){
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        } catch (ExpiredJwtException e) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Token has expired\"}");
        } catch (SignatureException e) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token signature\"}");
        } catch (MalformedJwtException e) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Malformed token\"}");
        } catch (UnsupportedJwtException e) { // May not be thrown directly by default parser setup
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Unsupported token type\"}");
        } catch (IllegalArgumentException e) { // e.g., if token string is empty or issues with key during parsing
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token argument (" + e.getMessage() + ")\"}");
        } catch (JwtException e) { // A general catch-all for other JWT-related issues
            // Log the actual exception for server-side debugging
            System.err.println("JWT Validation Error: " + e.getMessage());
            // e.printStackTrace(); // For more detailed logs
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token\"}");
        }
        return Optional.empty(); // Return empty if any exception occurred
    }
}