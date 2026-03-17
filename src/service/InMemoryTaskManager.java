package service;

import model.Status;
import model.Task;
import model.Subtask;
import model.Epic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements service.TaskManager {
    // Хеш-таблицы для хранения задач для классов Task, Epic, Subtask
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Менеджер истории
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Счетчик для генерации ID
    private int nextId = 1;

    // Метод для увеличения ID
    private int getNextId() {
        return nextId++;
    }

    // Получение списка всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаление всех задач для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
        // Чистка Эпиков, если подзадач больше нет
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            epic.setStatus(Status.NEW);
        }
    }

    // Получение по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) return null;

        historyManager.add(task);

        Task copy = new Task(task.getName(), task.getDescription(), task.getStatus());
        copy.setId(task.getId());

        return copy;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Возвращение списка истории
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Создание для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public Task createTask(Task task) {
        task.setId(getNextId());
        task.setStatus(Status.NEW);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epic.setStatus(Status.NEW);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        subtask.setStatus(Status.NEW);
        subtasks.put(subtask.getId(), subtask);

        // Добавляем в Эпик ID новой Подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }

        return subtask;
    }

    // Обновление задач для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public void updateTask(Task task) {
        // Проверка на наличие задачи
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
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

    @Override
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
    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаление подзадач, связанных с этим Эпиком
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }

    @Override
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
        historyManager.remove(id);
    }

    // Получение списка всех Подзадач для определённого Эпика
    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> tasks = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                tasks.add(subtasks.get(subtaskId));
            }
        }
        return tasks;
    }
}
