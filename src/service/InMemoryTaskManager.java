package service;

import model.Status;
import model.Task;
import model.Subtask;
import model.Epic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class InMemoryTaskManager implements service.TaskManager {
    // Хеш-таблицы для хранения задач для классов Task, Epic, Subtask
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Хранение задач и подзадач в отсортированном порядке по времени начала
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );

    // Менеджер истории
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Счетчик для генерации ID
    private int nextId = 1;

    // Получение следующего идентификатора задачи
    private int getNextId() {
        return nextId++;
    }

    // Получение списков задач, эпиков и подзадач
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

    // Удаление всех задач
    @Override
    public void clearTasks() {
        // Удаление каждой задачи из истории и списка приоритетов перед очисткой хранилища
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        // Сначала удаление всех подзадач из истории и списка приоритетов
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        // Затем самих эпиков
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        // Удаление каждой подзадачи из истории и списка приоритетов
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();

        // Очистка эпиков, если подзадач больше нет
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            epic.setStatus(Status.NEW);
        }
    }

    // Получение задачи, эпика или подзадачи по идентификатору
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }

        historyManager.add(task);

        Task copy = new Task(
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getDuration(),
                task.getStartTime()
        );
        copy.setId(task.getId());

        return copy;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }

        historyManager.add(epic);

        Epic copy = new Epic(epic.getName(), epic.getDescription());
        copy.setId(epic.getId());
        copy.setStatus(epic.getStatus());
        copy.setSubtaskIds(epic.getSubtaskIds());
        copy.setEpicDuration(epic.getDuration());
        copy.setEpicStartTime(epic.getStartTime());
        copy.setEpicEndTime(epic.getEndTime());

        return copy;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }

        historyManager.add(subtask);

        Subtask copy = new Subtask(
                subtask.getName(),
                subtask.getDescription(),
                subtask.getStatus(),
                subtask.getDuration(),
                subtask.getStartTime(),
                subtask.getEpicId()
        );
        copy.setId(subtask.getId());

        return copy;
    }

    // Получение списка истории
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Получение задач и подзадач в порядке приоритета по времени начала
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Добавление задачи или подзадачи в список приоритетов, если задано время начала
    protected void addTaskToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Создание задач, эпиков и подзадач
    @Override
    public Task createTask(Task task) {
        task.setId(getNextId());
        task.setStatus(Status.NEW);
        tasks.put(task.getId(), task);
        addTaskToPrioritizedTasks(task);

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
        addTaskToPrioritizedTasks(subtask);

        // Добавляем в Эпик ID новой Подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }

        return subtask;
    }

    // Обновление задачи с синхронизацией списка приоритетов
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());

            prioritizedTasks.remove(oldTask);
            tasks.put(task.getId(), task);
            addTaskToPrioritizedTasks(task);
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
    protected void updateEpicStatus(Epic epic) {
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
            Subtask oldSubtask = subtasks.get(subtask.getId());

            prioritizedTasks.remove(oldSubtask);
            subtasks.put(subtask.getId(), subtask);
            addTaskToPrioritizedTasks(subtask);

            // Пересчёт статуса эпика после обновления подзадачи
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    // Удаление по идентификатору для каждого из типов задач(Задача/Эпик/Подзадача)
    @Override
    public void deleteTask(int id) {
        Task removedTask = tasks.remove(id);
        if (removedTask != null) {
            prioritizedTasks.remove(removedTask);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаление подзадач, связанных с этим Эпиком
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask removedSubtask = subtasks.remove(subtaskId);
                if (removedSubtask != null) {
                    prioritizedTasks.remove(removedSubtask);
                }
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);

            // Удаление ID Подзадачи из Эпика
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
        historyManager.remove(id);
    }

    // Получение списка подзадач определённого эпика
    public List<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    // Служебные методы для восстановления менеджера из файла
    protected void putLoadedTask(Task task) {
        tasks.put(task.getId(), task);
        addTaskToPrioritizedTasks(task);
    }

    protected void putLoadedEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void putLoadedSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        addTaskToPrioritizedTasks(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }

    protected ArrayList<Epic> getLoadedEpics() {
        return new ArrayList<>(epics.values());
    }
}
