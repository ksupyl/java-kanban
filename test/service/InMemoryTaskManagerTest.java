package service;

import model.Status;
import model.Task;
import model.Epic;
import model.Subtask;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    //InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void addNewTask() {
        Task task = new Task("Task", "Task Description", Status.NEW);
        taskManager.createTask(task);

        final Task savedTask = taskManager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают");
    }

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.createEpic(epic);

        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void addNewSubtask() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        final Subtask savedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "У Подзадачи неверный EpicID");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
    }

    // Задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
    @Test
    void taskWithGeneratedIdShouldNotConflict() {
        Task task = new Task("Task", "Task Description", Status.NEW);
        task.setId(999);

        taskManager.createTask(task);

        assertNotEquals(999, task.getId(),
                "Менеджер должен игнорировать заданный вручную ID и генерировать уникальный");

        Task savedTask = taskManager.getTask(task.getId());
        assertNotNull(savedTask, "Задача должна быть найдена по сгенерированному ID");
        assertEquals(task, savedTask);
    }

    // Tест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskShouldBeUnchangedAfterAddingToManager() {
        // Эталонные данные
        String expectedName = "Orig Name";
        String expectedDescription = "Orig Description";
        Status expectedStatus = Status.NEW;

        Task task = new Task(expectedName, expectedDescription, expectedStatus);

        // Добавляем её
        taskManager.createTask(task);

        // Забираем
        Task savedTask = taskManager.getTask(task.getId());

        // Проверяем
        assertEquals(expectedName, savedTask.getName(), "Имя задачи изменилось при сохранении");
        assertEquals(expectedDescription, savedTask.getDescription(),
                "Описание задачи изменилось при сохранении");
        assertEquals(expectedStatus, savedTask.getStatus(), "Статус задачи изменился при сохранении");
    }

    // Если удалена задача, то она должна исчезнуть из истории
    @Test
    void deletedTaskShouldBeRemovedFromHistory() {
        Task task = new Task("Task", "Description", Status.NEW);
        taskManager.createTask(task);

        taskManager.getTask(task.getId());
        assertEquals(1, taskManager.getHistory().size(), "Задача должна быть в истории");

        taskManager.deleteTask(task.getId()); // удаляем
        assertEquals(0, taskManager.getHistory().size(),
                "Удалённая задача не должна оставаться в истории");
    }

    // При удалении эпика из истории удаляется и сам эпик, и все его подзадачи
    @Test
    void deletedEpicShouldBeRemovedFromHistoryWithSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Просматриваем всё — всё попадает в историю
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        assertEquals(3, taskManager.getHistory().size(), "В истории должно быть 3 элемента");

        taskManager.deleteEpic(epic.getId()); // удаляем эпик
        assertEquals(0, taskManager.getHistory().size(),
                "После удаления эпика история должна быть пустой");
    }

    // Если удалена подзадача, то её id не должен оставаться внутри эпика
    @Test
    void deletedSubtaskShouldBeRemovedFromEpic() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        int subtaskId = subtask.getId();
        taskManager.deleteSubtask(subtaskId);

        assertEquals(0, taskManager.getEpicSubtasks(epic.getId()).size(),
                "После удаления подзадачи эпик не должен содержать её id");
    }

    // Изменение задачи через сеттер не должно влиять на данные внутри менеджера
    @Test
    void taskShouldNotChangeInManagerAfterSetterCall() {
        Task task = new Task("Оригинальное имя", "Описание", Status.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());
        savedTask.setName("Изменённое имя");

        assertEquals("Оригинальное имя",
                taskManager.getTask(task.getId()).getName(),
                "Сеттер не должен менять данные задачи внутри менеджера");
    }

    // clearTasks() должен удалять задачи из истории
    @Test
    void clearTasksShouldRemoveTasksFromHistory() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        assertEquals(2, taskManager.getHistory().size(),
                "Перед очисткой в истории должно быть 2 задачи");

        taskManager.clearTasks();

        assertEquals(0, taskManager.getHistory().size(),
                "После clearTasks история должна быть пустой");
    }

    // clearEpics() должен удалять эпики и их подзадачи из истории
    @Test
    void clearEpicsShouldRemoveEpicsAndSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());
        assertEquals(2, taskManager.getHistory().size(),
                "Перед очисткой в истории должно быть 2 элемента");

        taskManager.clearEpics();

        assertEquals(0, taskManager.getHistory().size(),
                "После clearEpics история должна быть пустой");
    }

    // Изменение подзадачи через сеттер не должно влиять на данные внутри менеджера
    @Test
    void subtaskShouldNotChangeInManagerAfterSetterCall() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Оригинальное имя", "Описание", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtask(subtask.getId());
        savedSubtask.setName("Изменённое имя");

        assertEquals("Оригинальное имя",
                taskManager.getSubtask(subtask.getId()).getName(),
                "Сеттер не должен менять данные подзадачи внутри менеджера");
    }

    // clearSubtasks() должен удалять подзадачи из истории
    @Test
    void clearSubtasksShouldRemoveSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.getSubtask(subtask.getId()); // добавление в историю
        assertEquals(1, taskManager.getHistory().size(),
                "Перед очисткой в истории должна быть 1 подзадача");

        taskManager.clearSubtasks();

        assertEquals(0, taskManager.getHistory().size(),
                "После clearSubtasks история должна быть пустой");
    }
}