package http.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Базовый класс для всех HTTP-обработчиков.
// Содержит общие методы для отправки и чтения HTTP-данных.
public class BaseHttpHandler {

    // HTTP-статусы, используемые в обработчиках.
    protected static final int STATUS_OK = 200;
    protected static final int STATUS_CREATED = 201;
    protected static final int STATUS_NOT_FOUND = 404;
    protected static final int STATUS_NOT_ACCEPTABLE = 406;
    protected static final int STATUS_INTERNAL_ERROR = 500;

    // Отправка ответа с текстом в формате JSON.
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    // Отправка ответа 201 без тела.
    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(STATUS_CREATED, -1);
        exchange.close();
    }

    // Чтение тела HTTP-запроса в виде строки.
    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Ответ 404 — объект не найден.
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(STATUS_NOT_FOUND, -1);
        exchange.close();
    }

    // Ответ 406 — задача пересекается по времени.
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(STATUS_NOT_ACCEPTABLE, -1);
        exchange.close();
    }

    // Ответ 500 — внутренняя ошибка сервера.
    protected void sendInternalError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(STATUS_INTERNAL_ERROR, -1);
        exchange.close();
    }
}