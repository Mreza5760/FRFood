package org.FRFood.util;

import io.jsonwebtoken.*;
import org.FRFood.DAO.RestaurantDAO;
import org.FRFood.DAO.RestaurantDAOImp;
import org.FRFood.DAO.UserDAO;
import org.FRFood.entity.Restaurant;
import org.FRFood.entity.User;
import org.FRFood.DAO.UserDAOImp;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.security.SignatureException;

import java.util.Optional;
import java.io.IOException;
import java.sql.SQLException;

public class Authenticate {
    public static Optional<User> authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpError.unauthorized(exchange, "Missing or malformed Bearer token");
            return Optional.empty();
        }

        String token = authHeader.substring(7);

        try {
            Jws<Claims> claimsJws = JwtUtil.validateToken(token);
            int userId = Integer.parseInt(claimsJws.getBody().getSubject());

            UserDAO userDAO = new UserDAOImp();
            Optional<User> userOptional = userDAO.getById(userId);
            if (userOptional.isEmpty()) {
                HttpError.unauthorized(exchange, "User associated with token not found");
                return Optional.empty();
            }
            if(!userOptional.get().isConfirmed()){
                HttpError.forbidden(exchange, "you are not confirmed by Admin yet");
                return Optional.empty();
            }

            return userOptional;
        } catch (SQLException e) {
            HttpError.internal(exchange, "Database error while validating user");
        } catch (ExpiredJwtException e) {
            HttpError.unauthorized(exchange, "Token has expired");
        } catch (SignatureException e) {
            HttpError.unauthorized(exchange, "Invalid token signature");
        } catch (JwtException | IllegalArgumentException e) {
            HttpError.unauthorized(exchange, "Invalid token: " + e.getMessage());
        }

        return Optional.empty();
    }

    public static Optional<Restaurant> restaurantChecker(HttpExchange exchange, User user, int restaurantId) throws SQLException, IOException {
        RestaurantDAO restaurantDAO = new RestaurantDAOImp();
        Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(restaurantId);
        if (optionalRestaurant.isEmpty()) {
            HttpError.notFound(exchange, "Restaurant not found");
            return Optional.empty();
        }
        Restaurant restaurant = optionalRestaurant.get();
        if (restaurant.getOwner().getId() != user.getId()) {
            HttpError.unauthorized(exchange, "You do not own this restaurant");
            return Optional.empty();
        }
        return optionalRestaurant;
    }
}