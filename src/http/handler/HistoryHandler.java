package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

// Обработчик запросов по пути /history.
// Поддерживает получение истории просмотров задач.
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    // Менеджер задач, с которым работает обработчик.
    private final TaskManager taskManager;

    // Общий Gson для преобразования объектов в JSON.
    private final Gson gson = HttpTaskServer.getGson();

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Главный метод обработки HTTP-запроса.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (!"GET".equals(method)) {
                sendInternalError(exchange);
                return;
            }

            if (!"/history".equals(path)) {
                sendNotFound(exchange);
                return;
            }

            List<Task> history = taskManager.getHistory();
            String response = gson.toJson(history);
            sendText(exchange, response, 200);

        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}