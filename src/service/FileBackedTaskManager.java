package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            TreeMap<Integer, Task> allTasks = new TreeMap<>();

            for (Task task : getTasks()) {
                allTasks.put(task.getId(), task);
            }

            for (Epic epic : getEpics()) {
                allTasks.put(epic.getId(), epic);
            }

            for (Subtask subtask : getSubtasks()) {
                allTasks.put(subtask.getId(), subtask);
            }

            for (Task task : allTasks.values()) {
                writer.write(toString(task));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save tasks to file: " + file, e);
        }
    }

    private String toString(Task task) {
        StringBuilder builder = new StringBuilder();

        builder.append(task.getId()).append(",");

        if (task instanceof Subtask) {
            builder.append(TaskType.SUBTASK).append(",");
        } else if (task instanceof Epic) {
            builder.append(TaskType.EPIC).append(",");
        } else {
            builder.append(TaskType.TASK).append(",");
        }

        builder.append(task.getName()).append(",");
        builder.append(task.getStatus()).append(",");
        builder.append(task.getDescription()).append(",");

        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            builder.append(subtask.getEpicId());
        }

        return builder.toString();
    }
}
