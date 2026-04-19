package service;

import model.Status;
import model.Task;
import model.Subtask;
import model.Epic;
import service.exception.NotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class InMemoryTaskManager implements service.TaskManager {
    // Хеш-таблицы для хранения задач для классов Task, Epic, Subtask
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

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

    // Поиск обычной задачи по id
    private Task findTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с id=" + id + " не найдена.");
        }
        return task;
    }

    // Поиск эпика по id
    private Epic findEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + id + " не найден.");
        }
        return epic;
    }

    // Поиск подзадачи по id
    private Subtask findSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id=" + id + " не найдена.");
        }
        return subtask;
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
            updateEpicTime(epic);
        }
    }

    // Получение задачи, эпика или подзадачи по идентификатору
    @Override
    public Task getTask(int id) {
        Task task = findTaskById(id);

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
        Epic epic = findEpicById(id);

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
        Subtask subtask = findSubtaskById(id);

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

    // Проверка пересечения двух задач по времени выполнения
    protected boolean isTaskOverlapping(Task firstTask, Task secondTask) {
        return firstTask.getStartTime().isBefore(secondTask.getEndTime())
                && secondTask.getStartTime().isBefore(firstTask.getEndTime());
    }

    // Проверка пересечения задачи с уже существующими задачами и подзадачами
    protected boolean hasTimeOverlap(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .anyMatch(prioritizedTask ->
                        prioritizedTask.getId() != task.getId()
                                && isTaskOverlapping(task, prioritizedTask)
                );
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
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей.");
        }

        task.setId(getNextId());
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
        Epic epic = findEpicById(subtask.getEpicId());

        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей.");
        }

        subtask.setId(getNextId());
        subtask.setStatus(Status.NEW);
        subtasks.put(subtask.getId(), subtask);
        addTaskToPrioritizedTasks(subtask);

        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);

        return subtask;
    }

    // Обновление задачи с синхронизацией списка приоритетов
    @Override
    public void updateTask(Task task) {
        Task oldTask = findTaskById(task.getId());

        prioritizedTasks.remove(oldTask);

        if (hasTimeOverlap(task)) {
            addTaskToPrioritizedTasks(oldTask);
            throw new IllegalArgumentException("Обновлённая задача пересекается по времени с другой задачей.");
        }

        tasks.put(task.getId(), task);
        addTaskToPrioritizedTasks(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        // Создание копии списка задач во избежание потери данных при обновлении
        Epic oldEpic = findEpicById(epic.getId());
        epic.setSubtaskIds(oldEpic.getSubtaskIds());
        epic.setStatus(oldEpic.getStatus());
        epic.setEpicDuration(oldEpic.getDuration());
        epic.setEpicStartTime(oldEpic.getStartTime());
        epic.setEpicEndTime(oldEpic.getEndTime());

        epics.put(epic.getId(), epic);
    }

    // Пересчёт статуса эпика на основе его подзадач
    protected void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        long countNew = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStatus() == Status.NEW)
                .count();

        long countDone = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStatus() == Status.DONE)
                .count();

        if (countNew == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == epic.getSubtaskIds().size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    // Пересчёт времени эпика на основе его подзадач
    protected void updateEpicTime(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setEpicDuration(null);
            epic.setEpicStartTime(null);
            epic.setEpicEndTime(null);
            return;
        }

        long totalDurationInMinutes = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getDuration() != null)
                .mapToLong(subtask -> subtask.getDuration().toMinutes())
                .sum();

        LocalDateTime earliestStartTime = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStartTime)
                .filter(start -> start != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEndTime = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getEndTime)
                .filter(end -> end != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setEpicDuration(Duration.ofMinutes(totalDurationInMinutes));
        epic.setEpicStartTime(earliestStartTime);
        epic.setEpicEndTime(latestEndTime);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = findSubtaskById(subtask.getId());

        subtask.setEpicId(oldSubtask.getEpicId());

        Epic epic = findEpicById(subtask.getEpicId());

        prioritizedTasks.remove(oldSubtask);

        if (hasTimeOverlap(subtask)) {
            addTaskToPrioritizedTasks(oldSubtask);
            throw new IllegalArgumentException("Обновлённая подзадача пересекается по времени с другой задачей.");
        }

        subtasks.put(subtask.getId(), subtask);
        addTaskToPrioritizedTasks(subtask);

        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    // Удаление задачи, эпика или подзадачи по идентификатору
    @Override
    public void deleteTask(int id) {
        Task removedTask = findTaskById(id);

        tasks.remove(id);
        prioritizedTasks.remove(removedTask);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = findEpicById(id);

        epics.remove(id);

        // Удаление подзадач, связанных с этим эпиком
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask removedSubtask = subtasks.remove(subtaskId);
            if (removedSubtask != null) {
                prioritizedTasks.remove(removedSubtask);
            }
            historyManager.remove(subtaskId);
        }

        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = findSubtaskById(id);

        subtasks.remove(id);
        prioritizedTasks.remove(subtask);

        // Удаление id подзадачи из эпика
        Epic epic = findEpicById(subtask.getEpicId());
        epic.removeSubtaskId(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);

        historyManager.remove(id);
    }

    // Получение списка подзадач эпика с использованием Stream API
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = findEpicById(epicId);

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null)
                .toList();
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
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }

    protected ArrayList<Epic> getLoadedEpics() {
        return new ArrayList<>(epics.values());
    }
}
