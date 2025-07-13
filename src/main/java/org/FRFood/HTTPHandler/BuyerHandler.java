package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;

import org.FRFood.util.BuyerReq.ItemsReq;
import org.FRFood.util.BuyerReq.VendorsReq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public class BuyerHandler implements HttpHandler {
    private final FoodDAO foodDAO;
    private final PriceDAO priceDAO;
    private final ObjectMapper objectMapper;
    private final RestaurantDAO restaurantDAO;

    public BuyerHandler() {
        foodDAO = new FoodDAOImp();
        priceDAO = new PriceDAOImp();
        objectMapper = new ObjectMapper();
        restaurantDAO = new RestaurantDAOImp();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        // سوییچ کیس های داخلی هم نیازمند حالت دیفالت هستند
        try {
            switch (method) {
                case "POST" -> {
                    switch (path) {
                        case "/vendors" -> handleVendorsList(exchange);
                        case "/items" -> handleItemsList(exchange);
                    }
                }
                case "GET" -> {
                    switch (path) {
                        case "/vendors" -> handleVendorsMenu(exchange);
                        case "/items" -> handleGetItem(exchange);
                    }
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

    private void handleVendorsList(HttpExchange exchange) throws IOException {
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
            String json = objectMapper.writeValueAsString(restaurantsFiltered);
            JsonResponse.sendJsonResponse(exchange, 200, json);

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

    void handleVendorsMenu(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Restaurant> optionalRestaurant = restaurantDAO.getById(id);
            if (optionalRestaurant.isEmpty()) {
                // باید ارور وجود نداشتن رستوران داد
                return;
            }
            Restaurant restaurant = optionalRestaurant.get();

            /*
            اول اطلاعات رستوران
            بعد لیست اسم منو
            بعد لیست تمام غدا های داخل منو
             */
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void handleItemsList(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        ItemsReq req = objectMapper.readValue(exchange.getRequestBody(), ItemsReq.class);

        try {
            List<Food> foodsFiltered = new ArrayList<>();
            List<Restaurant> restaurants = restaurantDAO.searchByString(req.search);
            for (Restaurant restaurant : restaurants) {
                List<Food> foods = restaurantDAO.getFoods(restaurant.getId());
                for (Food food : foods) {
                    Optional<Price> optionalPrice = priceDAO.getById(food.getPriceId());
                    if (optionalPrice.isEmpty()) {
                        // باید ارور داد
                        return;
                    }
                    Price price = optionalPrice.get();
                    // تخفیف میشه هم لحاظ بشه هم نه
                    if (foodDAO.doesHaveKeywords(req.keywords) && price.getCurrentPrice() <= req.price) {
                        foodsFiltered.add(food);
                    }
                }
            }
            /*
            اینجا باید لیست غذا های فیلتر شده رو خروجی بدیم
             */
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }

    void  handleGetItem(HttpExchange exchange) throws IOException {
        Optional<User> authenticatedUserOptional = authenticate(exchange);
        if (authenticatedUserOptional.isEmpty()) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[2]);

        try {
            Optional<Food> optionalFood = foodDAO.getById(id);
            if (optionalFood.isEmpty()) {
                // باید ارور وجود نداشتن رستوران داد
                return;
            }
            Food food = optionalFood.get();

            /*
            اطلاعات این غذا رو باید پرینت کرد
             */
        } catch (SQLException e) {
//            e.printStackTrace();
            JsonResponse.sendJsonResponse(exchange, 500, "Database error");
        }
    }
}