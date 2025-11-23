import model.Status;
import model.Task;
import model.Epic;
import model.Subtask;
import service.TaskManager;

public class Main {

    // Метод для разделительной строки
    private static void printDelimiter(){
        System.out.println();
        System.out.println("-".repeat(30));
    }

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

        Subtask subtask3 = new Subtask("Подзадача 1", "Подзадача Эпика 2", Status.NEW, epic2.getId());
        manager.createSubtask(subtask3);

        // Печать списков Эпиков, Задач и Подзадач
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println();
        System.out.println("Эпики: " + manager.getEpics());
        System.out.println();
        System.out.println("Подзадачи: " + manager.getSubtasks());

        // Разделительная строка
        printDelimiter();

        // Изменение статуса Задачи 2
        task2.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task2);
        System.out.println("Задача 2: в процессе");
        System.out.println("Статус задачи 2: " + manager.getTask(task2.getId()).getStatus());

        // Разделительная строка
        printDelimiter();

        // Изменение статуса Подзадач в Эпике 1
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        System.out.println("Эпик 1: одна из Подзадач сделана и одна в процессе");
        System.out.println("Статус Эпика 1: " + manager.getEpic(epic1.getId()).getStatus());

        // Разделительная строка
        printDelimiter();

        // Продолжение изменения статуса Подзадач в Эпике 1
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);
        System.out.println("Эпик 1: обе Подзадачи сделаны");
        System.out.println("Статус Эпика 1: " + manager.getEpic(epic1.getId()).getStatus());

        // Разделительная строка
        printDelimiter();

        // Изменение статуса Подзадач в Эпике 2
        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);
        System.out.println("Эпик 2: одна Подзадача и она сделана");
        System.out.println("Статус Эпика 2: " + manager.getEpic(epic2.getId()).getStatus());

        // Разделительная строка
        printDelimiter();

        // Удаление задачи и эпика
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("Список Задач после удаления: " + manager.getTasks());
        System.out.println("Список Эпиков после удаления: " + manager.getEpics());
        System.out.println("Список Подзадач после удаления: " + manager.getSubtasks());
    }
}
