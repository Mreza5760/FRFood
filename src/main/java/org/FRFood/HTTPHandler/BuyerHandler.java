package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;

import org.FRFood.util.BuyerReq.VendorsReq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public class BuyerHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final RestaurantDAO restaurantDAO;
    private final FoodDAO foodDAO;

    public BuyerHandler() {
        objectMapper = new ObjectMapper();
        restaurantDAO = new RestaurantDAOImp();
        foodDAO = new FoodDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/vendors" -> handleVendors(exchange);
                    }
                }
                case "GET" -> {
                }
                case "PUT" -> {
                }
                default -> JsonResponse.sendJsonResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleVendors(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        VendorsReq req = objectMapper.readValue(exchange.getRequestBody(), VendorsReq.class);

        try {
            List<Restaurant> restaurantsFiltered = new ArrayList<>();
            List<Restaurant> restaurants = restaurantDAO.searchByString(req.search);
            for (Restaurant restaurant : restaurants) {
                boolean haveFood = false;
                List<Food> foods = restaurantDAO.getFoods(restaurant.getId());
                for (Food food : foods) {
                   if (foodDAO.doesHaveKeywords(req.keywords)) {
                       haveFood = true;
                       break;
                   }
                }
                if (haveFood) {
                    restaurantsFiltered.add(restaurant);
                }
            }
            /*
            مبین اینجا باید اون لیست لستوران فیلر شده رو خروجی بدی من بلد نیستم
            فقط قبلش فکر کنم باید توی انتیتی رستوران با جکسون عین یوزر تگ بزاری
            بعد نوشته بود یمل که تو خروجی لوگو اجباری نیست اونم چک کن که اگه رستوران لوگو نداشت
            یا نال بدی یا ندی لوگو رو
             */
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }


}