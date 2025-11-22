import model.Status;
import model.Task;
import model.Epic;
import model.Subtask;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание двух Задач
        Task task1 = new Task("Задача 1", "Описание 1 Задачи", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание 2 Задачи", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание Эпика с двумя Подзадачами
        Epic epic1 = new Epic("Эпик 1", "Эпик с 2 подзадачами");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Подзадача Эпика 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Подзадача Эпика 1", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Создание Эпика с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Эпик с 1 подзадачей");
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Подзадача Эпика 2", Status.NEW, epic2.getId());
        manager.createSubtask(subtask3);

        // Печать списков Эпиков, Задач и Подзадач
        System.out.println("Эпики: " + manager.getEpics());
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Подзадачи: " + manager.getSubtasks());
    }
}
