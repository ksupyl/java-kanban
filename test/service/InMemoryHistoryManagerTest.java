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

    // При повторном добавлении задачи в историю дубликат не создастся
    @Test
    void addShouldNotCreateDuplicates() {
        Task task = new Task("Task", "Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // добавляем второй раз
        historyManager.add(task); // и третий

        assertEquals(1, historyManager.getHistory().size(), "История не должна содержать дубликаты");
    }

    // История не должна ограничиваться 10 элементами
    @Test
    void historyShouldBeUnlimited() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description", Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        assertEquals(15, historyManager.getHistory().size(), "История должна хранить более 10 элементов");
    }

    // Удаление из начала истории
    @Test
    void removeShouldDeleteFromBeginning() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1); // удаление первой - головы

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(2, history.get(0).getId(), "Первой должна быть task2");
    }

    // Удаление из середины истории
    @Test
    void removeShouldDeleteFromMiddle() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2); // удаление из середины

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть task1");
        assertEquals(3, history.get(1).getId(), "Второй должна быть task3");
    }

    // Удаление из конца истории
    @Test
    void removeShouldDeleteFromEnd() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3); // удаление последней - хвоста

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории должно остаться 2 задачи");
        assertEquals(2, history.get(1).getId(), "Последней должна быть task2");
    }

    // Повторный просмотр задачи должен переместить её в конец истории
    @Test
    void repeatedViewShouldMoveTaskToEnd() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1); // task1 повторно — должна уйти в конец

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "Дубликатов быть не должно");
        assertEquals(2, history.get(0).getId(), "Первой должна быть task2");
        assertEquals(3, history.get(1).getId(), "Второй должна быть task3");
        assertEquals(1, history.get(2).getId(), "Последней должна быть task1");
    }
}