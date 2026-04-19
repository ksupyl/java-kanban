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

// Тесты для endpoint'а /prioritized.
public class HttpTaskManagerPrioritizedTest extends HttpTaskServerTestBase {

    // Проверка: GET /prioritized должен возвращать пустой список,
    // если задач с временем начала нет.
    @Test
    public void shouldReturnEmptyPrioritizedList() throws IOException, InterruptedException {
        URI url = createUri("/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа.");
        assertEquals("[]", response.body(), "Список приоритетных задач должен быть пустым.");
    }

    // Проверка: GET /prioritized должен возвращать задачи в порядке приоритета.
    @Test
    public void shouldReturnTasksInPrioritizedOrder() throws IOException, InterruptedException {
        Task lateTask = createTestTask(
                "Поздняя задача",
                "Будет второй",
                30,
                LocalDateTime.of(2026, 4, 27, 18, 0)
        );
        manager.createTask(lateTask);

        Task earlyTask = createTestTask(
                "Ранняя задача",
                "Будет первой",
                30,
                LocalDateTime.of(2026, 4, 27, 9, 0)
        );
        manager.createTask(earlyTask);

        Epic epic = createTestEpic("Эпик", "Описание эпика");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask middleSubtask = createTestSubtask(
                "Средняя подзадача",
                "Будет между задачами",
                45,
                LocalDateTime.of(2026, 4, 27, 13, 0),
                epicId
        );
        manager.createSubtask(middleSubtask);

        URI url = createUri("/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(200, response.statusCode(), "Некорректный код ответа при получении приоритетных задач.");
        assertEquals(3, jsonArray.size(), "В списке приоритетных задач должно быть три элемента.");

        JsonObject firstObject = jsonArray.get(0).getAsJsonObject();
        JsonObject secondObject = jsonArray.get(1).getAsJsonObject();
        JsonObject thirdObject = jsonArray.get(2).getAsJsonObject();

        assertEquals("Ранняя задача", firstObject.get("name").getAsString(),
                "Первой должна быть самая ранняя задача.");
        assertEquals("Средняя подзадача", secondObject.get("name").getAsString(),
                "Второй должна быть подзадача со средним временем.");
        assertEquals("Поздняя задача", thirdObject.get("name").getAsString(),
                "Третьей должна быть самая поздняя задача.");
    }

    // Проверка: GET /prioritized должен возвращать только GET-запросы.
    @Test
    public void shouldReturn500ForPostPrioritizedRequest() throws IOException, InterruptedException {
        URI url = createUri("/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(),
                "Для неподдерживаемого POST-запроса к /prioritized должен вернуться 500.");
    }
}