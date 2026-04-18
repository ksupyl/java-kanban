package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import model.Task;
import service.TaskManager;
import service.exception.NotFoundException;

import java.io.IOException;
import java.util.List;

// Обработчик запросов по пути /tasks.
// Поддерживает получение списка задач, получение задачи по id,
// создание/обновление задачи и удаление задачи.
public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    // Менеджер задач, с которым работает обработчик.
    private final TaskManager taskManager;

    // Общий Gson для преобразования объектов в JSON и обратно.
    private final Gson gson = HttpTaskServer.getGson();

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Главный метод обработчика HTTP-запроса.
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
    // GET /tasks      -> список всех задач
    // GET /tasks/{id} -> одна задача по id
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if ("/tasks".equals(path)) {
            List<Task> tasks = taskManager.getTasks();
            String response = gson.toJson(tasks);
            sendText(exchange, response, 200);
            return;
        }

        int id = extractId(path);
        Task task = taskManager.getTask(id);
        String response = gson.toJson(task);
        sendText(exchange, response, 200);
    }

    // Обработка POST-запроса:
    // POST /tasks -> создание новой задачи или обновление существующей.
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!"/tasks".equals(path)) {
            throw new NotFoundException("Некорректный путь для POST /tasks");
        }

        String body = readText(exchange);
        Task task = gson.fromJson(body, Task.class);

        if (task == null) {
            throw new IOException("Тело запроса пустое.");
        }

        if (task.getId() == 0) {
            taskManager.createTask(task);
        } else {
            taskManager.updateTask(task);
        }

        sendCreated(exchange);
    }

    // Обработка DELETE-запроса:
    // DELETE /tasks/{id} -> удаление задачи по id.
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        taskManager.deleteTask(id);
        sendText(exchange, "", 200);
    }

    // Извлечение id задачи из пути.
    // Ожидается путь вида /tasks/{id}.
    private int extractId(String path) {
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            throw new NotFoundException("Некорректный путь запроса.");
        }

        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Некорректный идентификатор задачи.");
        }
    }
}