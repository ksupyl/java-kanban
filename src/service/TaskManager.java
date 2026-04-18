package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // Получение списка всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    // Удаление всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    // Получение по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // История просмотров задач
    List<Task> getHistory();

    // Создание для каждого из типов задач(Задача/Эпик/Подзадача)
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    // Обновление задач для каждого из типов задач(Задача/Эпик/Подзадача)
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    // Удаление по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // Получение подзадач эпика по id
    List<Subtask> getEpicSubtasks(int epicId);

    // Получение задач и подзадач в порядке приоритета (по startTime)
    List<Task> getPrioritizedTasks();
}
