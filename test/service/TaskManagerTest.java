package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    // Каждый наследник сам создаёт свою реализацию менеджера
    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    // Проверка создания обычной задачи
    @Test
    void shouldCreateTask() {
        Task task = new Task(
                "Task name",
                "Task description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        );

        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask, "Задача должна создаться.");
        assertNotNull(taskManager.getTask(createdTask.getId()), "Задача должна находиться по id.");
        assertEquals("Task name", createdTask.getName(), "Имя задачи должно сохраниться.");
        assertEquals(Duration.ofMinutes(30), createdTask.getDuration(),
                "Продолжительность задачи должна сохраниться.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 0), createdTask.getStartTime(),
                "Время начала задачи должно сохраниться.");
    }

    // Проверка создания эпика
    @Test
    void shouldCreateEpic() {
        Epic epic = new Epic("Epic name", "Epic description");

        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic, "Эпик должен создаться.");
        assertNotNull(taskManager.getEpic(createdEpic.getId()), "Эпик должен находиться по id.");
        assertEquals("Epic name", createdEpic.getName(), "Имя эпика должно сохраниться.");
    }

    // Проверка создания подзадачи и связи с эпиком
    @Test
    void shouldCreateSubtaskWithEpic() {
        Epic epic = new Epic("Epic", "Epic description");
        Epic createdEpic = taskManager.createEpic(epic);

        Subtask subtask = new Subtask(
                "Subtask",
                "Subtask description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 12, 0),
                createdEpic.getId()
        );

        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask, "Подзадача должна создаться.");
        assertEquals(createdEpic.getId(), createdSubtask.getEpicId(),
                "Подзадача должна хранить id связанного эпика.");

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(createdEpic.getId());
        assertEquals(1, epicSubtasks.size(), "У эпика должна быть одна подзадача.");
        assertEquals(createdSubtask.getId(), epicSubtasks.get(0).getId(),
                "Подзадача должна входить в список подзадач эпика.");
    }

    // Проверка расчёта статуса эпика: все подзадачи NEW
    @Test
    void shouldSetEpicStatusNewWhenAllSubtasksAreNew() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask(
                "Subtask 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        taskManager.createSubtask(new Subtask(
                "Subtask 2",
                "Description 2",
                Status.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        Epic savedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.NEW, savedEpic.getStatus(),
                "Статус эпика должен быть NEW, если все подзадачи NEW.");
    }

    // Проверка расчёта статуса эпика: все подзадачи DONE
    @Test
    void shouldSetEpicStatusDoneWhenAllSubtasksAreDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask(
                "Subtask 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        Subtask subtask2 = taskManager.createSubtask(new Subtask(
                "Subtask 2",
                "Description 2",
                Status.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.DONE, savedEpic.getStatus(),
                "Статус эпика должен быть DONE, если все подзадачи DONE.");
    }

    // Проверка расчёта статуса эпика: подзадачи NEW и DONE
    @Test
    void shouldSetEpicStatusInProgressWhenSubtasksAreNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask(
                "Subtask 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        Subtask subtask2 = taskManager.createSubtask(new Subtask(
                "Subtask 2",
                "Description 2",
                Status.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        subtask2.setStatus(Status.DONE);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(),
                "Статус эпика должен быть IN_PROGRESS, если подзадачи NEW и DONE.");
    }

    // Проверка расчёта статуса эпика: есть подзадача IN_PROGRESS
    @Test
    void shouldSetEpicStatusInProgressWhenSubtaskIsInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        subtask.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask);

        Epic savedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(),
                "Статус эпика должен быть IN_PROGRESS, если есть подзадача IN_PROGRESS.");
    }

    // Проверка приоритетного списка задач
    @Test
    void shouldReturnPrioritizedTasksSortedByStartTime() {
        Task laterTask = taskManager.createTask(new Task(
                "Later task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 12, 0)
        ));

        Task earlierTask = taskManager.createTask(new Task(
                "Earlier task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 9, 0)
        ));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "Должно быть две задачи в списке приоритетов.");
        assertEquals(earlierTask.getId(), prioritizedTasks.get(0).getId(),
                "Первая задача должна быть с более ранним startTime.");
        assertEquals(laterTask.getId(), prioritizedTasks.get(1).getId(),
                "Вторая задача должна быть с более поздним startTime.");
    }

    // Проверка, что задача без startTime не попадает в приоритетный список
    @Test
    void shouldNotAddTaskWithoutStartTimeToPrioritizedTasks() {
        Task taskWithoutTime = new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                null
        );

        taskManager.createTask(taskWithoutTime);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.isEmpty(),
                "Задача без startTime не должна попадать в список приоритетов.");
    }

    // Проверка пересечения интервалов при создании задачи
    @Test
    void shouldThrowExceptionWhenCreatingOverlappingTask() {
        taskManager.createTask(new Task(
                "Task 1",
                "Description",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.createTask(new Task(
                        "Task 2",
                        "Description",
                        Status.NEW,
                        Duration.ofMinutes(30),
                        LocalDateTime.of(2026, 4, 16, 10, 30)
                )),
                "Создание пересекающейся задачи должно приводить к исключению."
        );
    }

    // Проверка времени эпика по подзадачам
    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask(
                "Subtask 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        taskManager.createSubtask(new Subtask(
                "Subtask 2",
                "Description 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 12, 0),
                epic.getId()
        ));

        Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Duration.ofMinutes(90), savedEpic.getDuration(),
                "Продолжительность эпика должна быть суммой продолжительностей подзадач.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 0), savedEpic.getStartTime(),
                "Время начала эпика должно быть временем начала самой ранней подзадачи.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 12, 30), savedEpic.getEndTime(),
                "Время окончания эпика должно быть временем окончания самой поздней подзадачи.");
    }
}