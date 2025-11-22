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
}
