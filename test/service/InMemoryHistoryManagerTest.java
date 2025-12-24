package service;

import model.Status;
import model.Task;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    // Задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных
    @Test
    void add() {
        Task task = new Task("Task", "Task Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

        assertEquals(task.getName(), history.get(0).getName(), "Имя задачи в истории не совпадает");
        assertEquals(task.getDescription(), history.get(0).getDescription(), "Описание задачи в истории не совпадает");
        assertEquals(task.getStatus(), history.get(0).getStatus(), "Статус задачи в истории не совпадает");
        assertEquals(task.getId(), history.get(0).getId(), "ID задачи в истории не совпадает");
    }
}