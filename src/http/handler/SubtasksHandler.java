package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import model.Subtask;
import service.TaskManager;
import service.exception.NotFoundException;

import java.io.IOException;
import java.util.List;

// Обработчик запросов по пути /subtasks.
// Поддерживает получение списка подзадач, получение подзадачи по id,
// создание/обновление подзадачи и удаление подзадачи.
public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    // Менеджер задач, с которым работает обработчик.
    private final TaskManager taskManager;

    // Общий Gson для преобразования объектов в JSON и обратно.
    private final Gson gson = HttpTaskServer.getGson();

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Главный метод обработки HTTP-запроса.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                handleGet(exchange, path);
                return;
            }

            if ("POST".equals(method)) {
                handlePost(exchange, path);
                return;
            }

            if ("DELETE".equals(method)) {
                handleDelete(exchange, path);
                return;
            }

            sendInternalError(exchange);

        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    // Обработка GET-запросов:
    // GET /subtasks      -> список всех подзадач
    // GET /subtasks/{id} -> одна подзадача по id
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if ("/subtasks".equals(path)) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            String response = gson.toJson(subtasks);
            sendText(exchange, response, STATUS_OK);
            return;
        }

        int id = extractId(path);
        Subtask subtask = taskManager.getSubtask(id);
        String response = gson.toJson(subtask);
        sendText(exchange, response, STATUS_OK);
    }

    // Обработка POST-запроса:
    // POST /subtasks -> создание новой подзадачи или обновление существующей.
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!"/subtasks".equals(path)) {
            throw new NotFoundException("Некорректный путь для POST /subtasks");
        }

        String body = readText(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask == null) {
            throw new IOException("Тело запроса пустое.");
        }

        if (subtask.getId() == 0) {
            taskManager.createSubtask(subtask);
        } else {
            taskManager.updateSubtask(subtask);
        }

        sendCreated(exchange);
    }

    // Обработка DELETE-запроса:
    // DELETE /subtasks/{id} -> удаление подзадачи по id.
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        taskManager.deleteSubtask(id);
        sendText(exchange, "", STATUS_OK);
    }

    // Извлечение id подзадачи из пути.
    // Ожидается путь вида /subtasks/{id}.
    private int extractId(String path) {
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            throw new NotFoundException("Некорректный путь запроса.");
        }

        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Некорректный идентификатор подзадачи.");
        }
    }
}