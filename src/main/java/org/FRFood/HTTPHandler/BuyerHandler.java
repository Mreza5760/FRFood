package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import io.jsonwebtoken.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.security.SignatureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BuyerHandler implements HttpHandler {
    private final ObjectMapper objectMapper;

    public BuyerHandler() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST" -> {
//                    switch (path) {
//                        case "/auth/register" -> handleRegister(exchange);
//                        case "/auth/login" -> handleLogin(exchange);
//                        case "/auth/logout" -> handleLogout(exchange);
//                        default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
//                    }
                }
                case "GET" -> {
//                    if (path.equals("/auth/profile")) {
//                        handleGetProfile(exchange);
//                    } else {
//                        JsonResponse.sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
//                    }
                }
                case "PUT" -> {
//                    if (path.equals("/auth/profile")) {
//                        handleUpdateProfile(exchange);
//                    } else {
//                        JsonResponse.sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
//                    }
                }
                default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }
}