package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Тесты для endpoint'а /epics.
public class HttpTaskManagerEpicsTest extends HttpTaskServerTestBase {

    // Проверка: GET /epics должен возвращать пустой список,
    // если эпиков в менеджере нет.
    @Test
    public void shouldReturnEmptyEpicsList() throws IOException, InterruptedException {
        URI url = createUri("/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа.");
        assertEquals("[]", response.body(), "Список эпиков должен быть пустым.");
    }

    // Проверка: POST /epics должен создавать новый эпик.
    @Test
    public void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Покупки", "Список покупок на вечер");
        String epicJson = gson.toJson(epic);

        URI url = createUri("/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Epic> epicsFromManager = manager.getEpics();

        assertEquals(201, response.statusCode(), "Некорректный код ответа при создании эпика.");
        assertNotNull(epicsFromManager, "Список эпиков не должен быть null.");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков.");
        assertEquals("Покупки", epicsFromManager.get(0).getName(), "Некорректное имя эпика.");
    }

    // Проверка: POST /epics должен обновлять эпик,
    // если в JSON указан существующий id.
    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Учёба", "Старое описание");
        manager.createEpic(epic);

        Epic savedEpic = manager.getEpics().get(0);

        Epic updatedEpic = createTestEpic("Учёба и практика", "Новое описание");
        updatedEpic.setId(savedEpic.getId());

        String updatedEpicJson = gson.toJson(updatedEpic);

        URI url = createUri("/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic epicFromManager = manager.getEpic(savedEpic.getId());

        assertEquals(201, response.statusCode(), "Некорректный код ответа при обновлении эпика.");
        assertEquals("Учёба и практика", epicFromManager.getName(), "Имя эпика не обновилось.");
        assertEquals("Новое описание", epicFromManager.getDescription(), "Описание эпика не обновилось.");
    }

    // Проверка: POST /epics должен возвращать 404,
    // если в JSON указан id несуществующего эпика.
    @Test
    public void shouldReturn404WhenUpdatingMissingEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Несуществующий эпик", "Не должен обновиться");
        epic.setId(999);

        String epicJson = gson.toJson(epic);

        URI url = createUri("/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при попытке обновить несуществующий эпик.");
    }

    // Проверка: GET /epics/{id} должен возвращать эпик по id.
    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Учёба", "Подготовка к экзамену");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        URI url = createUri("/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic epicFromResponse = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode(), "Некорректный код ответа при получении эпика по id.");
        assertNotNull(epicFromResponse, "Эпик не пришёл в ответе.");
        assertEquals(epicId, epicFromResponse.getId(), "Некорректный id эпика.");
        assertEquals("Учёба", epicFromResponse.getName(), "Некорректное имя эпика.");
    }

    // Проверка: GET /epics/{id} должен возвращать 404,
    // если эпика с таким id не существует.
    @Test
    public void shouldReturn404WhenEpicNotFound() throws IOException, InterruptedException {
        URI url = createUri("/epics/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Должен вернуться статус 404, если эпик не найден.");
    }

    // Проверка: GET /epics/{id}/subtasks должен возвращать список подзадач эпика.
    @Test
    public void shouldReturnEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Переезд", "Подготовка к переезду");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Собрать коробки",
                "Упаковать вещи",
                60,
                LocalDateTime.of(2026, 4, 22, 10, 0),
                epicId
        );
        manager.createSubtask(subtask);

        URI url = createUri("/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        JsonObject firstSubtask = jsonArray.get(0).getAsJsonObject();

        assertEquals(200, response.statusCode(),
                "Некорректный код ответа при получении подзадач эпика.");
        assertEquals(1, jsonArray.size(), "У эпика должна быть одна подзадача.");
        assertEquals("Собрать коробки", firstSubtask.get("name").getAsString(),
                "Некорректное имя подзадачи в ответе.");
    }

    // Проверка: GET /epics/{id}/subtasks должен возвращать 404,
    // если эпика с таким id не существует.
    @Test
    public void shouldReturn404WhenEpicSubtasksRequestedForMissingEpic() throws IOException, InterruptedException {
        URI url = createUri("/epics/" + MISSING_ID + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404, если подзадачи запрашиваются у несуществующего эпика.");
    }

    // Проверка: DELETE /epics/{id} должен удалять эпик.
    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Отпуск", "Подготовка к отпуску");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        URI url = createUri("/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа при удалении эпика.");
        assertTrue(manager.getEpics().isEmpty(), "Эпик не был удалён из менеджера.");
    }

    // Проверка: DELETE /epics/{id} должен возвращать 404,
    // если эпика с таким id не существует.
    @Test
    public void shouldReturn404WhenDeletingMissingEpic() throws IOException, InterruptedException {
        URI url = createUri("/epics/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при удалении несуществующего эпика.");
    }
}