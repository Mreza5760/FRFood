package org.FRFood.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpExchange;

public class HttpError {
    public static void send(HttpExchange exchange, int code, String errorMessage) throws IOException {
        String json = "{\"error\":\"" + errorMessage.replace("\"", "\\\"") + "\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    public static void badRequest(HttpExchange ex, String msg) throws IOException { send(ex, 400, msg); }
    public static void unauthorized(HttpExchange ex, String msg) throws IOException { send(ex, 401, msg); }
    public static void forbidden(HttpExchange ex, String msg) throws IOException { send(ex, 403, msg); }
    public static void notFound(HttpExchange ex, String msg) throws IOException { send(ex, 404, msg); }
    public static void conflict(HttpExchange ex, String msg) throws IOException { send(ex, 409, msg); }
    public static void unsupported(HttpExchange ex, String msg) throws IOException { send(ex, 415, msg); }
    public static void tooManyRequests(HttpExchange ex, String msg) throws IOException { send(ex, 429, msg); }
    public static void internal(HttpExchange ex, String msg) throws IOException { send(ex, 500, msg); }
}