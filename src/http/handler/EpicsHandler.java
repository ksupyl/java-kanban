package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import model.Epic;
import model.Subtask;
import service.TaskManager;
import service.exception.NotFoundException;

import java.io.IOException;
import java.util.List;

// Обработчик запросов по пути /epics.
// Поддерживает получение списка эпиков, получение эпика по id,
// получение подзадач эпика, создание/обновление эпика и удаление эпика.
public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    // Менеджер задач, с которым работает обработчик.
    private final TaskManager taskManager;

    // Общий Gson для преобразования объектов в JSON и обратно.
    private final Gson gson = HttpTaskServer.getGson();

    public EpicsHandler(TaskManager taskManager) {
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
    // GET /epics -> список всех эпиков
    // GET /epics/{id} -> один эпик по id
    // GET /epics/{id}/subtasks -> список подзадач эпика
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if ("/epics".equals(path)) {
            List<Epic> epics = taskManager.getEpics();
            String response = gson.toJson(epics);
            sendText(exchange, response, STATUS_OK);
            return;
        }

        if (path.endsWith("/subtasks")) {
            int epicId = extractEpicIdForSubtasks(path);
            List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
            String response = gson.toJson(subtasks);
            sendText(exchange, response, STATUS_OK);
            return;
        }

        int id = extractId(path);
        Epic epic = taskManager.getEpic(id);
        String response = gson.toJson(epic);
        sendText(exchange, response, STATUS_OK);
    }

    // Обработка POST-запроса:
    // POST /epics -> создание нового эпика или обновление существующего.
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!"/epics".equals(path)) {
            throw new NotFoundException("Некорректный путь для POST /epics");
        }

        String body = readText(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic == null) {
            throw new IOException("Тело запроса пустое.");
        }

        if (epic.getId() == 0) {
            taskManager.createEpic(epic);
        } else {
            taskManager.updateEpic(epic);
        }

        sendCreated(exchange);
    }

    // Обработка DELETE-запроса:
    // DELETE /epics/{id} -> удаление эпика по id.
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        taskManager.deleteEpic(id);
        sendText(exchange, "", STATUS_OK);
    }

    // Извлечение id эпика из пути вида /epics/{id}.
    private int extractId(String path) {
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            throw new NotFoundException("Некорректный путь запроса.");
        }

        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Некорректный идентификатор эпика.");
        }
    }

    // Извлечение id эпика из пути вида /epics/{id}/subtasks.
    private int extractEpicIdForSubtasks(String path) {
        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {
            throw new NotFoundException("Некорректный путь запроса.");
        }

        if (!"subtasks".equals(pathParts[3])) {
            throw new NotFoundException("Некорректный путь запроса.");
        }

        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Некорректный идентификатор эпика.");
        }
    }
}