package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Тесты для endpoint'а /history.
public class HttpTaskManagerHistoryTest extends HttpTaskServerTestBase {

    // Проверка: GET /history должен возвращать пустой список,
    // если история просмотров пуста.
    @Test
    public void shouldReturnEmptyHistory() throws IOException, InterruptedException {
        URI url = createUri("/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа.");
        assertEquals("[]", response.body(), "История должна быть пустой.");
    }

    // Проверка: GET /history должен возвращать просмотренные задачи.
    @Test
    public void shouldReturnViewedTasksInHistory() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Обычная задача",
                "Описание задачи",
                30,
                LocalDateTime.of(2026, 4, 26, 10, 0)
        );
        manager.createTask(task);

        Epic epic = createTestEpic("Эпик", "Описание эпика");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Подзадача",
                "Описание подзадачи",
                40,
                LocalDateTime.of(2026, 4, 26, 12, 0),
                epicId
        );
        manager.createSubtask(subtask);

        int taskId = manager.getTasks().get(0).getId();
        int subtaskId = manager.getSubtasks().get(0).getId();

        manager.getTask(taskId);
        manager.getEpic(epicId);
        manager.getSubtask(subtaskId);

        URI url = createUri("/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(200, response.statusCode(), "Некорректный код ответа при получении истории.");
        assertEquals(3, jsonArray.size(), "В истории должно быть три просмотренные сущности.");

        JsonObject firstObject = jsonArray.get(0).getAsJsonObject();
        JsonObject secondObject = jsonArray.get(1).getAsJsonObject();
        JsonObject thirdObject = jsonArray.get(2).getAsJsonObject();

        assertEquals(taskId, firstObject.get("id").getAsInt(), "Первой в истории должна быть обычная задача.");
        assertEquals(epicId, secondObject.get("id").getAsInt(), "Вторым в истории должен быть эпик.");
        assertEquals(subtaskId, thirdObject.get("id").getAsInt(), "Третьей в истории должна быть подзадача.");
    }

    // Проверка: GET /history должен возвращать только GET-запросы.
    @Test
    public void shouldReturn500ForPostHistoryRequest() throws IOException, InterruptedException {
        URI url = createUri("/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(),
                "Для неподдерживаемого POST-запроса к /history должен вернуться 500.");
    }
}