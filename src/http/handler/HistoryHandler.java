package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;

// Обработчик запросов по пути /history.
// Пока содержит только базовую заготовку, логика будет добавлена позже.
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        sendText(exchange, "\"History endpoint is not implemented yet\"", 200);
    }
}