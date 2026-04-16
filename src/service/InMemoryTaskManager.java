package service;

import model.Status;
import model.Task;
import model.Subtask;
import model.Epic;

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

    // Проверка пересечения двух задач по времени выполнения
    protected boolean isTaskOverlapping(Task firstTask, Task secondTask) {
        if (firstTask.getStartTime() == null || secondTask.getStartTime() == null) {
            return false;
        }

        if (firstTask.getEndTime() == null || secondTask.getEndTime() == null) {
            return false;
        }

        return firstTask.getStartTime().isBefore(secondTask.getEndTime())
                && secondTask.getStartTime().isBefore(firstTask.getEndTime());
    }

    // Проверка пересечения задачи с уже существующими задачами и подзадачами
    protected boolean hasTimeOverlap(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(prioritizedTask -> prioritizedTask.getId() != task.getId()
                        && isTaskOverlapping(task, prioritizedTask));
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
        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей.");
        }

        subtask.setId(getNextId());
        subtask.setStatus(Status.NEW);
        subtasks.put(subtask.getId(), subtask);
        addTaskToPrioritizedTasks(subtask);

        // Добавляем в эпик id новой подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }

        return subtask;
    }

    // Обновление задачи с синхронизацией списка приоритетов
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());

            prioritizedTasks.remove(oldTask);

            if (hasTimeOverlap(task)) {
                addTaskToPrioritizedTasks(oldTask);
                throw new IllegalArgumentException("Обновлённая задача пересекается по времени с другой задачей.");
            }

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

    // Пересчёт статуса эпика на основе его подзадач
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

    // Пересчёт времени эпика на основе его подзадач
    protected void updateEpicTime(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setEpicDuration(null);
            epic.setEpicStartTime(null);
            epic.setEpicEndTime(null);
            return;
        }

        long totalDurationInMinutes = 0;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            if (subtask.getDuration() != null) {
                totalDurationInMinutes += subtask.getDuration().toMinutes();
            }

            if (subtask.getStartTime() != null) {
                if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }
            }

            if (subtask.getEndTime() != null) {
                if (latestEndTime == null || subtask.getEndTime().isAfter(latestEndTime)) {
                    latestEndTime = subtask.getEndTime();
                }
            }
        }

        epic.setEpicDuration(Duration.ofMinutes(totalDurationInMinutes));
        epic.setEpicStartTime(earliestStartTime);
        epic.setEpicEndTime(latestEndTime);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());

            prioritizedTasks.remove(oldSubtask);

            if (hasTimeOverlap(subtask)) {
                addTaskToPrioritizedTasks(oldSubtask);
                throw new IllegalArgumentException("Обновлённая подзадача пересекается по времени с другой задачей.");
            }

            subtasks.put(subtask.getId(), subtask);
            addTaskToPrioritizedTasks(subtask);

            // Пересчёт статуса эпика после обновления подзадачи
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
        }
    }

    // Удаление задачи, эпика или подзадачи по идентификатору
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
                updateEpicTime(epic);
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
