package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(file);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл для теста.", e);
        }
    }

    // Проверка сохранения и загрузки пустого менеджера
    @Test
    void shouldSaveAndLoadEmptyManager() {
        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = createTaskManager();
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым.");
            assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым.");
            assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым.");
        }, "Загрузка пустого менеджера не должна выбрасывать исключение.");
    }

    // Проверка сохранения и загрузки задачи с полями времени
    @Test
    void shouldSaveAndLoadTaskWithTimeFields() {
        FileBackedTaskManager manager = createTaskManager();

        Task task = new Task(
                "Task",
                "Task description",
                Status.NEW,
                Duration.ofMinutes(45),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        );

        Task createdTask = manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTask(createdTask.getId());

        assertNotNull(loadedTask, "Задача должна загрузиться из файла.");
        assertEquals(Duration.ofMinutes(45), loadedTask.getDuration(),
                "Продолжительность задачи должна сохраниться.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 0), loadedTask.getStartTime(),
                "Время начала задачи должно сохраниться.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 45), loadedTask.getEndTime(),
                "Время окончания задачи должно корректно рассчитываться после загрузки.");
    }

    // Проверка сохранения и загрузки времени эпика через подзадачи
    @Test
    void shouldSaveAndLoadEpicTimeCalculatedFromSubtasks() {
        FileBackedTaskManager manager = createTaskManager();

        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));

        manager.createSubtask(new Subtask(
                "Subtask 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        manager.createSubtask(new Subtask(
                "Subtask 2",
                "Description 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 12, 0),
                epic.getId()
        ));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertNotNull(loadedEpic, "Эпик должен загрузиться из файла.");
        assertEquals(Duration.ofMinutes(90), loadedEpic.getDuration(),
                "Продолжительность эпика должна восстановиться по подзадачам.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 9, 0), loadedEpic.getStartTime(),
                "Время начала эпика должно восстановиться по самой ранней подзадаче.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 12, 30), loadedEpic.getEndTime(),
                "Время окончания эпика должно восстановиться по самой поздней подзадаче.");
    }

    // Проверка исключения при загрузке несуществующего файла
    @Test
    void shouldThrowExceptionWhenLoadingFromInvalidFile() {
        assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(new File("file_does_not_exist.csv")),
                "Загрузка несуществующего файла должна приводить к ManagerSaveException."
        );
    }
}