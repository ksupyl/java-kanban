package http;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Тесты для endpoint'а /subtasks.
public class HttpTaskManagerSubtasksTest extends HttpTaskServerTestBase {

    // Проверка: GET /subtasks должен возвращать пустой список,
    // если подзадач в менеджере нет.
    @Test
    public void shouldReturnEmptySubtasksList() throws IOException, InterruptedException {
        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа.");
        assertEquals("[]", response.body(), "Список подзадач должен быть пустым.");
    }

    // Проверка: POST /subtasks должен создавать новую подзадачу,
    // если эпик существует.
    @Test
    public void shouldCreateSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Домашние дела", "Список домашних дел");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Помыть окна",
                "На кухне и в комнате",
                45,
                LocalDateTime.of(2026, 4, 23, 14, 0),
                epicId
        );

        String subtaskJson = gson.toJson(subtask);

        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertEquals(201, response.statusCode(), "Некорректный код ответа при создании подзадачи.");
        assertNotNull(subtasksFromManager, "Список подзадач не должен быть null.");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач.");
        assertEquals("Помыть окна", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи.");
    }

    // Проверка: POST /subtasks должен обновлять подзадачу,
    // если в JSON указан существующий id.
    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Учёба", "Подготовка к занятиям");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Сделать домашку",
                "Первая версия",
                60,
                LocalDateTime.of(2026, 4, 25, 16, 0),
                epicId
        );
        manager.createSubtask(subtask);

        Subtask savedSubtask = manager.getSubtasks().get(0);

        Subtask updatedSubtask = createTestSubtask(
                "Сделать домашку по Java",
                "Обновлённая версия",
                90,
                LocalDateTime.of(2026, 4, 25, 18, 0),
                epicId
        );
        updatedSubtask.setId(savedSubtask.getId());

        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask subtaskFromManager = manager.getSubtask(savedSubtask.getId());

        assertEquals(201, response.statusCode(), "Некорректный код ответа при обновлении подзадачи.");
        assertEquals("Сделать домашку по Java", subtaskFromManager.getName(), "Имя подзадачи не обновилось.");
        assertEquals("Обновлённая версия", subtaskFromManager.getDescription(),
                "Описание подзадачи не обновилось.");
    }

    // Проверка: POST /subtasks должен возвращать 404,
    // если в JSON указан id несуществующей подзадачи.
    @Test
    public void shouldReturn404WhenUpdatingMissingSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Работа", "Рабочий эпик");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Несуществующая подзадача",
                "Не должна обновиться",
                40,
                LocalDateTime.of(2026, 4, 26, 11, 0),
                epicId
        );
        subtask.setId(999);

        String subtaskJson = gson.toJson(subtask);

        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при попытке обновить несуществующую подзадачу.");
    }

    // Проверка: GET /subtasks/{id} должен возвращать подзадачу по id.
    @Test
    public void shouldReturnSubtaskById() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Учёба", "Подготовка к занятиям");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Решить задачи",
                "Практика по Java",
                90,
                LocalDateTime.of(2026, 4, 23, 18, 0),
                epicId
        );
        manager.createSubtask(subtask);

        int subtaskId = manager.getSubtasks().get(0).getId();

        URI url = createUri("/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask subtaskFromResponse = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode(), "Некорректный код ответа при получении подзадачи по id.");
        assertNotNull(subtaskFromResponse, "Подзадача не пришла в ответе.");
        assertEquals(subtaskId, subtaskFromResponse.getId(), "Некорректный id подзадачи.");
        assertEquals("Решить задачи", subtaskFromResponse.getName(), "Некорректное имя подзадачи.");
    }

    // Проверка: GET /subtasks/{id} должен возвращать 404,
    // если подзадачи с таким id не существует.
    @Test
    public void shouldReturn404WhenSubtaskNotFound() throws IOException, InterruptedException {
        URI url = createUri("/subtasks/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Должен вернуться статус 404, если подзадача не найдена.");
    }

    // Проверка: POST /subtasks должен возвращать 404,
    // если у подзадачи указан несуществующий epicId.
    @Test
    public void shouldReturn404WhenCreatingSubtaskWithoutExistingEpic() throws IOException, InterruptedException {
        Subtask subtask = createTestSubtask(
                "Несуществующий эпик",
                "Подзадача не должна создаться",
                30,
                LocalDateTime.of(2026, 4, 24, 10, 0),
                999
        );

        String subtaskJson = gson.toJson(subtask);

        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404, если подзадача создаётся без существующего эпика.");
        assertTrue(manager.getSubtasks().isEmpty(), "Подзадача не должна сохраниться в менеджере.");
    }

    // Проверка: POST /subtasks должен возвращать 406,
    // если новая подзадача пересекается по времени с существующей задачей.
    @Test
    public void shouldReturn406WhenSubtaskHasTimeOverlap() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Работа", "Рабочие задачи");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Task task = createTestTask(
                "Основная задача",
                "Занимает время",
                60,
                LocalDateTime.of(2026, 4, 24, 12, 0)
        );
        manager.createTask(task);

        Subtask overlappingSubtask = createTestSubtask(
                "Пересекающаяся подзадача",
                "Не должна сохраниться",
                30,
                LocalDateTime.of(2026, 4, 24, 12, 30),
                epicId
        );

        String subtaskJson = gson.toJson(overlappingSubtask);

        URI url = createUri("/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "Должен вернуться статус 406 при пересечении подзадачи по времени.");
        assertTrue(manager.getSubtasks().isEmpty(), "Пересекающаяся подзадача не должна сохраниться.");
    }

    // Проверка: DELETE /subtasks/{id} должен удалять подзадачу.
    @Test
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Проект", "Работа над проектом");
        manager.createEpic(epic);

        int epicId = manager.getEpics().get(0).getId();

        Subtask subtask = createTestSubtask(
                "Написать код",
                "Основной модуль",
                120,
                LocalDateTime.of(2026, 4, 25, 11, 0),
                epicId
        );
        manager.createSubtask(subtask);

        int subtaskId = manager.getSubtasks().get(0).getId();

        URI url = createUri("/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа при удалении подзадачи.");
        assertTrue(manager.getSubtasks().isEmpty(), "Подзадача не была удалена из менеджера.");
    }

    // Проверка: DELETE /subtasks/{id} должен возвращать 404,
    // если подзадачи с таким id не существует.
    @Test
    public void shouldReturn404WhenDeletingMissingSubtask() throws IOException, InterruptedException {
        URI url = createUri("/subtasks/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при удалении несуществующей подзадачи.");
    }
}