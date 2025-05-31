package org.FRFood.HTTPHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FRFood.entity.*;
import org.FRFood.util.*;
import org.FRFood.DAO.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Auth implements HttpHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equals("POST")) {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/auth/register")) {
                handleRegister(exchange);
            } else if (path.equals("/auth/login")) {
                handleLogin(exchange);
            } else {
                sendJsonResponse(exchange, 404, "Not Found");
            }
        }else if(exchange.getRequestMethod().equals("GET")){

        }else if(exchange.getRequestMethod().equals("PUT")){

        }else{
            sendJsonResponse(exchange, 404, "Not Found");
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE); // Important for JSON naming match

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
            UserDAO userDAO = new UserDAOImp();
            BankAccountDAO bankDAO = new BankAccountDAOImp();

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
//        User loginRequest = objectMapper.readValue(exchange.getRequestBody(), User.class);
//
//        String sql = "SELECT * FROM Users WHERE phone = ?";
//        try (Connection conn = DatabaseConnector.gConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, loginRequest.getPhoneNumber());
//            ResultSet rs = stmt.executeQuery();
//
//            if (!rs.next() || !rs.getString("password_hash").equals(loginRequest.getPassword())) {
//                send(exchange, 401, "Invalid credentials");
//                return;
//            }
//
//            // Token generation
//            User user = new User();
//            user.setId(rs.getInt("id"));
//            user.setFullName(rs.getString("full_name"));
//            user.setPhoneNumber(rs.getString("phone"));
//            user.setEmail(rs.getString("email"));
//            user.setRole(null); // Add if role is needed
//            String token = JwtUtil.generateToken(user);
//
//            send(exchange, 200, "{\"token\":\"" + token + "\"}");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            send(exchange, 500, "Database error");
//        }
    }

}