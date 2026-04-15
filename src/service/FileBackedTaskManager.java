package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Сохраняет текущее состояние менеджера в CSV-файл
    private void save() {
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

    // Преобразует задачу в строку формата CSV
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

    // Преобразует строку CSV в объект задачи
    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);

        int id = Integer.parseInt(fields[0]);
        TaskType taskType = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Task task;

        if (taskType == TaskType.TASK) {
            task = new Task(name, description, status);
        } else if (taskType == TaskType.EPIC) {
            task = new Epic(name, description);
            task.setStatus(status);
        } else {
            int epicId = Integer.parseInt(fields[5]);
            task = new Subtask(name, description, status, epicId);
        }

        task.setId(id);
        return task;
    }

    // Восстанавливает менеджер из CSV-файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // пропуск заголовка

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                Task task = fromString(line);

                if (task instanceof Subtask) {
                    manager.putLoadedSubtask((Subtask) task);
                } else if (task instanceof Epic) {
                    manager.putLoadedEpic((Epic) task);
                } else {
                    manager.putLoadedTask(task);
                }

                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
            }

            for (Epic epic : manager.getLoadedEpics()) {
                manager.updateEpicStatus(epic);
            }

            manager.setNextId(maxId + 1);
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load tasks from file: " + file, e);
        }
    }

    // После каждого изменения сохраняем состояние менеджера в файл
    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    // Демонстрационный сценарий работы файлового менеджера
    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создание обычных задач
        Task task1 = new Task("Task1", "Description of task 1", Status.NEW);
        Task task2 = new Task("Task2", "Description of task 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика
        Epic epic1 = new Epic("Epic1", "Description of epic 1");
        manager.createEpic(epic1);

        // Создание подзадач для эпика
        Subtask subtask1 = new Subtask("Subtask1", "Description of subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Description of subtask 2", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Меняем статус одной подзадачи для проверки пересчёта эпика
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        // Загрузка нового менеджера из того же файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Вывод данных из нового менеджера
        System.out.println("Обычные задачи из загруженного менеджера:");
        for (Task task : loadedManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики из загруженного менеджера:");
        for (Epic epic : loadedManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("Подзадачи из загруженного менеджера:");
        for (Subtask subtask : loadedManager.getSubtasks()) {
            System.out.println(subtask);
        }

        // Проверка, что данные действительно загрузились
        if (loadedManager.getTasks().size() == manager.getTasks().size()
                && loadedManager.getEpics().size() == manager.getEpics().size()
                && loadedManager.getSubtasks().size() == manager.getSubtasks().size()) {
            System.out.println("Данные успешно сохранены и загружены.");
        } else {
            System.out.println("Ошибка: данные после загрузки не совпадают.");
        }
    }
}
