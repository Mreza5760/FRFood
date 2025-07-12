package org.FRFood.util;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.FRFood.DAO.UserDAO;
import org.FRFood.DAO.UserDAOImp;
import org.FRFood.entity.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class Authenticate {
    public static Optional<User> authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Missing or malformed Bearer token\"}");
            return Optional.empty();
        }

        String token = authHeader.substring(7);

        try {
            Jws<Claims> claimsJws = JwtUtil.validateToken(token);
            Claims claims = claimsJws.getBody();

            int userId = Integer.parseInt(claims.getSubject());
            // String userRoleFromToken = claims.get("role", String.class);

            UserDAO userDAO = new UserDAOImp();
            Optional<User> userOptional = userDAO.getById(userId);

            if (userOptional.isEmpty()) {
                JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: User associated with token not found\"}");
                return Optional.empty();
            }

            return userOptional;
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        } catch (ExpiredJwtException e) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Token has expired\"}");
        } catch (SignatureException e) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token signature\"}");
        } catch (MalformedJwtException e) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Malformed token\"}");
        } catch (UnsupportedJwtException e) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Unsupported token type\"}");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token argument (" + e.getMessage() + ")\"}");
        } catch (JwtException e) {
            System.err.println("JWT Validation Error: " + e.getMessage());
            // e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid token\"}");
        }
        return Optional.empty();
    }
}