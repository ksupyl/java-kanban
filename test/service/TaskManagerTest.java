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

    // Проверка очистки всех задач
    @Test
    void shouldClearAllTasks() {
        Task task = taskManager.createTask(new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        taskManager.getTask(task.getId());
        taskManager.clearTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым.");
        assertNull(taskManager.getTask(task.getId()), "Задача не должна находиться после очистки.");
        assertTrue(taskManager.getHistory().isEmpty(), "История должна очищаться от удалённых задач.");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Приоритетный список должен очищаться.");
    }

    // Проверка очистки всех подзадач и сброса полей эпика
    @Test
    void shouldClearAllSubtasksAndResetEpicFields() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        taskManager.clearSubtasks();

        Epic savedEpic = taskManager.getEpic(epic.getId());

        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым.");
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "У эпика не должно остаться подзадач.");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика должен стать NEW.");
        assertNull(savedEpic.getStartTime(), "Время начала эпика должно стать null.");
        assertNull(savedEpic.getEndTime(), "Время окончания эпика должно стать null.");
        assertNull(savedEpic.getDuration(), "Продолжительность эпика должна стать null.");
    }

    // Проверка очистки всех эпиков и связанных подзадач
    @Test
    void shouldClearAllEpicsAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 11, 0),
                epic.getId()
        ));

        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());

        taskManager.clearEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым.");
        assertNull(taskManager.getEpic(epic.getId()), "Эпик не должен находиться после очистки.");
        assertNull(taskManager.getSubtask(subtask.getId()), "Подзадача не должна находиться после очистки.");
        assertTrue(taskManager.getHistory().isEmpty(), "История должна очищаться.");
    }

    // Проверка обновления задач, подзадач и эпиков
    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask(new Task(
                "Old name",
                "Old description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        Task updatedTask = new Task(
                "New name",
                "New description",
                Status.IN_PROGRESS,
                Duration.ofMinutes(45),
                LocalDateTime.of(2026, 4, 16, 12, 0)
        );
        updatedTask.setId(task.getId());

        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTask(task.getId());

        assertEquals("New name", savedTask.getName());
        assertEquals("New description", savedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, savedTask.getStatus());
        assertEquals(Duration.ofMinutes(45), savedTask.getDuration());
        assertEquals(LocalDateTime.of(2026, 4, 16, 12, 0), savedTask.getStartTime());
    }

    @Test
    void shouldUpdateEpicWithoutLosingCalculatedFields() {
        Epic epic = taskManager.createEpic(new Epic("Old epic", "Old description"));

        taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        Epic updatedEpic = new Epic("New epic", "New description");
        updatedEpic.setId(epic.getId());

        taskManager.updateEpic(updatedEpic);

        Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals("New epic", savedEpic.getName());
        assertEquals("New description", savedEpic.getDescription());
        assertEquals(1, savedEpic.getSubtaskIds().size(), "Связь с подзадачами должна сохраниться.");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус не должен теряться.");
        assertEquals(Duration.ofMinutes(30), savedEpic.getDuration(), "Продолжительность не должна теряться.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 0), savedEpic.getStartTime(), "StartTime не должен теряться.");
        assertEquals(LocalDateTime.of(2026, 4, 16, 10, 30), savedEpic.getEndTime(), "EndTime не должен теряться.");
    }

    @Test
    void shouldUpdateSubtaskAndRecalculateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Old subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 9, 0),
                epic.getId()
        ));

        Subtask updatedSubtask = new Subtask(
                "New subtask",
                "New description",
                Status.DONE,
                Duration.ofMinutes(40),
                LocalDateTime.of(2026, 4, 16, 11, 0),
                epic.getId()
        );
        updatedSubtask.setId(subtask.getId());

        taskManager.updateSubtask(updatedSubtask);

        Subtask savedSubtask = taskManager.getSubtask(subtask.getId());
        Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals("New subtask", savedSubtask.getName());
        assertEquals(Status.DONE, savedSubtask.getStatus());
        assertEquals(Duration.ofMinutes(40), savedSubtask.getDuration());
        assertEquals(LocalDateTime.of(2026, 4, 16, 11, 0), savedSubtask.getStartTime());

        assertEquals(Status.DONE, savedEpic.getStatus(), "Статус эпика должен пересчитаться.");
        assertEquals(Duration.ofMinutes(40), savedEpic.getDuration(), "Продолжительность эпика должна пересчитаться.");
    }

    // Проверка получения списка подзадач эпика
    @Test
    void shouldReturnEmptyListForEpicWithoutSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertTrue(epicSubtasks.isEmpty(), "У нового эпика список подзадач должен быть пустым.");
    }

    @Test
    void shouldReturnEmptyListForNonExistingEpicSubtasks() {
        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(999);

        assertTrue(epicSubtasks.isEmpty(), "Для несуществующего эпика должен возвращаться пустой список.");
    }

    // Проверка сортировки задач и подзадач в приоритетном списке
    @Test
    void shouldReturnTasksAndSubtasksSortedByStartTime() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Task task = taskManager.createTask(new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 12, 0)
        ));

        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "В приоритетном списке должны быть задача и подзадача.");
        assertEquals(subtask.getId(), prioritizedTasks.get(0).getId(), "Подзадача с более ранним startTime должна быть первой.");
        assertEquals(task.getId(), prioritizedTasks.get(1).getId(), "Задача с более поздним startTime должна быть второй.");
    }

    // Проверка удаления задач, подзадач и эпиков
    @Test
    void shouldDeleteTaskById() {
        Task task = taskManager.createTask(new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        taskManager.getTask(task.getId());
        taskManager.deleteTask(task.getId());

        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена.");
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым.");
        assertTrue(taskManager.getHistory().isEmpty(), "История должна очищаться от удалённой задачи.");
    }

    @Test
    void shouldDeleteSubtaskAndUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        taskManager.deleteSubtask(subtask.getId());

        Epic savedEpic = taskManager.getEpic(epic.getId());

        assertNull(taskManager.getSubtask(subtask.getId()), "Подзадача должна быть удалена.");
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "У эпика не должно остаться подзадач.");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика должен пересчитаться.");
        assertNull(savedEpic.getDuration(), "Продолжительность эпика должна стать null.");
    }

    @Test
    void shouldDeleteEpicWithItsSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                epic.getId()
        ));

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()), "Эпик должен быть удалён.");
        assertNull(taskManager.getSubtask(subtask.getId()), "Подзадача эпика тоже должна быть удалена.");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым.");
    }

    // Проверка граничных случаев поиска задач, эпиков и подзадач
    @Test
    void shouldReturnNullWhenTaskNotFound() {
        assertNull(taskManager.getTask(999), "Несуществующая задача должна возвращать null.");
    }

    @Test
    void shouldReturnNullWhenEpicNotFound() {
        assertNull(taskManager.getEpic(999), "Несуществующий эпик должен возвращать null.");
    }

    @Test
    void shouldReturnNullWhenSubtaskNotFound() {
        assertNull(taskManager.getSubtask(999), "Несуществующая подзадача должна возвращать null.");
    }

    // Проверка добавления задач в историю просмотров
    @Test
    void shouldAddViewedTaskToHistory() {
        Task task = taskManager.createTask(new Task(
                "Task",
                "Description",
                Status.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.of(2026, 4, 16, 8, 0)
        ));

        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size(), "После просмотра задача должна попасть в историю.");
        assertEquals(task.getId(), history.get(0).getId(), "В истории должна быть просмотренная задача.");
    }

    // Проверка создания подзадачи без существующего эпика
    @Test
    void shouldNotCreateSubtaskWithoutExistingEpic() {
        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 10, 0),
                999
        );

        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNull(createdSubtask, "Подзадача не должна создаваться без существующего эпика.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен остаться пустым.");
    }

    // Проверка пересечения временных интервалов задач и подзадач
    @Test
    void shouldAllowTasksThatTouchBordersButDoNotOverlap() {
        taskManager.createTask(new Task(
                "Task 1",
                "Description",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        assertDoesNotThrow(() -> taskManager.createTask(new Task(
                "Task 2",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 16, 11, 0)
        )), "Задачи, соприкасающиеся границами, не должны считаться пересекающимися.");
    }

    @Test
    void shouldThrowExceptionWhenCreatingOverlappingSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createTask(new Task(
                "Task 1",
                "Description",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 16, 10, 0)
        ));

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask(new Subtask(
                "Subtask",
                "Description",
                Status.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 16, 10, 30),
                epic.getId()
        )), "Подзадача не должна создаваться, если пересекается по времени с другой задачей.");
    }
}