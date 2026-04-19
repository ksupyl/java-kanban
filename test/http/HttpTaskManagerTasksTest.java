package http;

import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Тесты для endpoint'а /tasks.
public class HttpTaskManagerTasksTest extends HttpTaskServerTestBase {

    // Проверка: GET /tasks должен возвращать пустой список,
    // если задач в менеджере нет.
    @Test
    public void shouldReturnEmptyTasksList() throws IOException, InterruptedException {
        URI url = createUri("/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа.");
        assertEquals("[]", response.body(), "Список задач должен быть пустым.");
    }

    // Проверка: POST /tasks должен создавать новую задачу.
    @Test
    public void shouldCreateTask() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Закодить 9 спринт",
                "по Java",
                30,
                LocalDateTime.of(2026, 4, 18, 19, 0)
        );

        String taskJson = gson.toJson(task);

        URI url = createUri("/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Task> tasksFromManager = manager.getTasks();

        assertEquals(201, response.statusCode(), "Некорректный код ответа при создании задачи.");
        assertNotNull(tasksFromManager, "Задачи не возвращаются.");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач.");
        assertEquals("Закодить 9 спринт", tasksFromManager.get(0).getName(), "Некорректное имя задачи.");
    }

    // Проверка: GET /tasks/{id} должен возвращать задачу по id.
    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Почитать книгу",
                "Перед сном",
                40,
                LocalDateTime.of(2026, 4, 18, 21, 0)
        );
        manager.createTask(task);

        int taskId = manager.getTasks().get(0).getId();

        URI url = createUri("/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task taskFromResponse = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Некорректный код ответа при получении задачи по id.");
        assertNotNull(taskFromResponse, "Задача не пришла в ответе.");
        assertEquals(taskId, taskFromResponse.getId(), "Некорректный id задачи.");
        assertEquals("Почитать книгу", taskFromResponse.getName(), "Некорректное имя задачи.");
    }

    // Проверка: GET /tasks/{id} должен возвращать 404,
    // если задачи с таким id не существует.
    @Test
    public void shouldReturn404WhenTaskNotFound() throws IOException, InterruptedException {
        URI url = createUri("/tasks/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Должен вернуться статус 404, если задача не найдена.");
    }

    // Проверка: POST /tasks должен обновлять задачу,
    // если в JSON указан существующий id.
    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Сделать зарядку",
                "Утром",
                15,
                LocalDateTime.of(2026, 4, 19, 8, 0)
        );
        manager.createTask(task);

        Task savedTask = manager.getTasks().get(0);

        Task updatedTask = createTestTask(
                "Сделать зарядку дома",
                "Утром после пробуждения",
                20,
                LocalDateTime.of(2026, 4, 19, 8, 30)
        );
        updatedTask.setId(savedTask.getId());

        String updatedTaskJson = gson.toJson(updatedTask);

        URI url = createUri("/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task taskFromManager = manager.getTask(savedTask.getId());

        assertEquals(201, response.statusCode(), "Некорректный код ответа при обновлении задачи.");
        assertEquals("Сделать зарядку дома", taskFromManager.getName(), "Имя задачи не обновилось.");
        assertEquals("Утром после пробуждения", taskFromManager.getDescription(),
                "Описание задачи не обновилось.");
    }

    // Проверка: POST /tasks должен возвращать 404,
    // если в JSON указан id несуществующей задачи.
    @Test
    public void shouldReturn404WhenUpdatingMissingTask() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Несуществующая задача",
                "Не должна обновиться",
                20,
                LocalDateTime.of(2026, 4, 20, 15, 0)
        );
        task.setId(999);

        String taskJson = gson.toJson(task);

        URI url = createUri("/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при попытке обновить несуществующую задачу.");
    }

    // Проверка: POST /tasks должен возвращать 406,
    // если новая задача пересекается по времени с существующей.
    @Test
    public void shouldReturn406WhenTaskHasTimeOverlap() throws IOException, InterruptedException {
        Task firstTask = createTestTask(
                "Первая задача",
                "Описание первой задачи",
                60,
                LocalDateTime.of(2026, 4, 20, 10, 0)
        );
        manager.createTask(firstTask);

        Task secondTask = createTestTask(
                "Вторая задача",
                "Описание второй задачи",
                30,
                LocalDateTime.of(2026, 4, 20, 10, 30)
        );

        String secondTaskJson = gson.toJson(secondTask);

        URI url = createUri("/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(secondTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "Должен вернуться статус 406 при пересечении задач по времени.");
        assertEquals(1, manager.getTasks().size(), "В менеджере не должна сохраниться пересекающаяся задача.");
    }

    // Проверка: DELETE /tasks/{id} должен удалять задачу.
    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = createTestTask(
                "Удаляемая задача",
                "Нужно удалить",
                25,
                LocalDateTime.of(2026, 4, 21, 12, 0)
        );
        manager.createTask(task);

        int taskId = manager.getTasks().get(0).getId();

        URI url = createUri("/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа при удалении задачи.");
        assertTrue(manager.getTasks().isEmpty(), "Задача не была удалена из менеджера.");
    }

    // Проверка: DELETE /tasks/{id} должен возвращать 404,
    // если задачи с таким id не существует.
    @Test
    public void shouldReturn404WhenDeletingMissingTask() throws IOException, InterruptedException {
        URI url = createUri("/tasks/" + MISSING_ID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Должен вернуться статус 404 при удалении несуществующей задачи.");
    }
}