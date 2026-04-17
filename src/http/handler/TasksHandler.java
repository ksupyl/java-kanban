package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;

// Обработчик запросов по пути /tasks.
// Пока содержит только базовую заготовку, логика будет добавлена позже.
public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        sendText(exchange, "\"Tasks endpoint is not implemented yet\"", 200);
    }
}