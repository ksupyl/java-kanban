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


}
