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
}
