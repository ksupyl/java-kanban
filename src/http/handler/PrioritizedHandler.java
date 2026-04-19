package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

// Обработчик запросов по пути /prioritized.
// Поддерживает получение списка задач в порядке приоритета.
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    // Менеджер задач, с которым работает обработчик.
    private final TaskManager taskManager;

    // Общий Gson для преобразования объектов в JSON.
    private final Gson gson = HttpTaskServer.getGson();

    public PrioritizedHandler(TaskManager taskManager) {
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

            if (!"/prioritized".equals(path)) {
                sendNotFound(exchange);
                return;
            }

            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            String response = gson.toJson(prioritizedTasks);
            sendText(exchange, response, STATUS_OK);

        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}