package service;

import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    // Проверка пустой истории
    @Test
    void shouldReturnEmptyHistoryWhenNoTasksWereAdded() {
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null.");
        assertTrue(history.isEmpty(), "История должна быть пустой.");
    }

    // Проверка добавления задачи в историю
    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        );
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task.getId(), history.get(0).getId(), "В истории должна быть добавленная задача.");
    }

    // Проверка удаления дубликатов в истории
    @Test
    void shouldKeepOnlyOneTaskWhenTaskAddedTwice() {
        Task task = new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        );
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Дубликаты не должны сохраняться в истории.");
        assertEquals(task.getId(), history.get(0).getId(), "В истории должна остаться одна задача.");
    }

    // Проверка удаления задачи из начала истории
    @Test
    void shouldRemoveTaskFromBeginningOfHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 4, 16, 9, 0));
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 16, 10, 0));
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления в истории должна остаться одна задача.");
        assertEquals(2, history.get(0).getId(), "В истории должна остаться вторая задача.");
    }

    // Проверка удаления задачи из середины истории
    @Test
    void shouldRemoveTaskFromMiddleOfHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 4, 16, 9, 0));
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 16, 10, 0));
        task2.setId(2);

        Task task3 = new Task("Task 3", "Description 3", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 4, 16, 11, 0));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления в истории должно остаться две задачи.");
        assertEquals(1, history.get(0).getId(), "Первая задача должна остаться.");
        assertEquals(3, history.get(1).getId(), "Третья задача должна остаться.");
    }

    // Проверка удаления задачи из конца истории
    @Test
    void shouldRemoveTaskFromEndOfHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 4, 16, 9, 0));
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 16, 10, 0));
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления в истории должна остаться одна задача.");
        assertEquals(1, history.get(0).getId(), "В истории должна остаться первая задача.");
    }

    // Проверка сохранения новых полей во snapshot истории
    @Test
    void shouldSaveTimeFieldsInHistorySnapshot() {
        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(40),
                LocalDateTime.of(2026, 4, 16, 13, 0),
                100
        );
        subtask.setId(1);

        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История должна содержать одну подзадачу.");
        Task savedTask = history.get(0);

        assertEquals(Duration.ofMinutes(40), savedTask.getDuration(),
                "Продолжительность должна сохраниться в snapshot истории.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 13, 0), savedTask.getStartTime(),
                "Время начала должно сохраниться в snapshot истории.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 13, 40), savedTask.getEndTime(),
                "Время окончания должно корректно рассчитываться в snapshot истории.");
    }
}