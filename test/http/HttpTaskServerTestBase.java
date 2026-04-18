package http;

import com.google.gson.Gson;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;

// Базовый класс для HTTP-тестов.
// Содержит общий код запуска и остановки сервера,
// а также общие объекты, которые пригодятся в наследниках.
public abstract class HttpTaskServerTestBase {

    // Менеджер задач, который будет использоваться сервером в тестах.
    protected TaskManager manager;

    // Экземпляр HTTP-сервера.
    protected HttpTaskServer taskServer;

    // Gson сервера для преобразования объектов в JSON и обратно.
    protected Gson gson;

    // HTTP-клиент для отправки запросов к локальному серверу.
    protected HttpClient client;

    // Запуск нового чистого окружения перед каждым тестом.
    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();

        taskServer.start();
    }

    // Остановка сервера после каждого теста.
    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    // Вспомогательный метод для быстрого создания обычной задачи.
    protected Task createTestTask(String name, String description, int durationMinutes, LocalDateTime startTime) {
        return new Task(
                name,
                description,
                Status.NEW,
                Duration.ofMinutes(durationMinutes),
                startTime
        );
    }

    // Вспомогательный метод для быстрого создания эпика.
    protected Epic createTestEpic(String name, String description) {
        return new Epic(name, description);
    }

    // Вспомогательный метод для быстрого создания подзадачи.
    protected Subtask createTestSubtask(String name, String description, int durationMinutes,
                                        LocalDateTime startTime, int epicId) {
        return new Subtask(
                name,
                description,
                Status.NEW,
                Duration.ofMinutes(durationMinutes),
                startTime,
                epicId
        );
    }
}