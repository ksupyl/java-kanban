package service;

// Импорт классов из пакета model
import model.Status; // перечисляемый тип enum Status
import model.Task; // класс Task
import model.Subtask; // класс Subtask
import model.Epic; // класс Epic

import java.util.ArrayList; // импорт списка
import java.util.HashMap; // импорт хеш-таблиц

public class TaskManager {
    // Хеш-таблицы для хранения задач для классов Task, Epic, Subtask
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Счетчик для генерации ID
    private int nextId = 1;

    // Метод для увеличения ID
    private int getNextId() {
        return nextId++;
    }

    // Получение списка всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаление всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
        // Чистка Эпиков, если подзадач больше нет
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            epic.setStatus(Status.NEW);
        }
    }

    // Получение по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    // Создание для каждого из типов задач(Задача/Эпик/Подзадача)
    public Task createTask(Task task) {
        task.setId(getNextId());
        task.setStatus(Status.NEW);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epic.setStatus(Status.NEW);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        subtask.setStatus(Status.NEW);
        subtasks.put(subtask.getId(), subtask);

        // Добавляем в Эпик ID новой Подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }

        return subtask;
    }

    // Обновление задач для каждого из типов задач(Задача/Эпик/Подзадача)
    public void updateTask(Task task) {
        // Проверка на наличие задачи
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            // Создание копии списка задач во избежание потери данных при обновлении
            Epic oldEpic = epics.get(epic.getId());
            epic.setSubtaskIds(oldEpic.getSubtaskIds());
            epic.setStatus(oldEpic.getStatus());

            epics.put(epic.getId(), epic);
        }
    }

    // Метод для обновления статуса Эпик при обновлении Подзадач
    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int countNew = 0;
        int countDone = 0;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStatus() == Status.NEW) {
                countNew++;
            } else if (subtask.getStatus() == Status.DONE) {
                countDone++;
            }
        }

        if (countNew == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            // Пересчёт статуса Эпик после обновления Подзадач
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    // Удаление по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаление подзадач, связанных с этим Эпиком
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            // Удаление ID Подзадачи из Эпика
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }
}
