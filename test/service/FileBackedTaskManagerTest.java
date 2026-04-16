package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    // Тест на сохранение и загрузку пустого менеджера
    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();
        }

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    // Тест на сохранение нескольких задач
    @Test
    void shouldSaveMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task = new Task("Task1", "Description1", Status.NEW);
        manager.createTask(task);

        Epic epic = new Epic("Epic1", "Description epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "Description sub", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loadedManager.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна быть 1 подзадача");
    }

    // Тест на загрузку нескольких задач из файла
    @Test
    void shouldLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task = new Task("Task1", "Description1", Status.NEW);
        manager.createTask(task);

        Epic epic = new Epic("Epic1", "Description epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "Description sub", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask = loadedManager.getTasks().get(0);
        Epic loadedEpic = loadedManager.getEpics().get(0);
        Subtask loadedSubtask = loadedManager.getSubtasks().get(0);

        assertEquals("Task1", loadedTask.getName(), "Имя задачи должно совпадать");
        assertEquals("Description1", loadedTask.getDescription(), "Описание задачи должно совпадать");
        assertEquals(Status.NEW, loadedTask.getStatus(), "Статус задачи должен совпадать");

        assertEquals("Epic1", loadedEpic.getName(), "Имя эпика должно совпадать");
        assertEquals("Description epic", loadedEpic.getDescription(), "Описание эпика должно совпадать");
        assertEquals(Status.DONE, loadedEpic.getStatus(), "Статус эпика должен пересчитаться по подзадаче");

        assertEquals("Subtask1", loadedSubtask.getName(), "Имя подзадачи должно совпадать");
        assertEquals("Description sub", loadedSubtask.getDescription(), "Описание подзадачи должно совпадать");
        assertEquals(Status.DONE, loadedSubtask.getStatus(), "Статус подзадачи должен совпадать");
        assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Epic ID подзадачи должен совпадать");

        assertEquals(task.getId(), loadedTask.getId(), "ID задачи должен совпадать");
        assertEquals(epic.getId(), loadedEpic.getId(), "ID эпика должен совпадать");
        assertEquals(subtask.getId(), loadedSubtask.getId(), "ID подзадачи должен совпадать");
    }
}
