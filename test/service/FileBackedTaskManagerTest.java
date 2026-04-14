package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    // Тест на сохранение и загрузку пустого менеджера
    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        manager.save();

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

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loadedManager.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна быть 1 подзадача");
    }
}
