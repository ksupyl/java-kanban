import model.Status;
import model.Task;
import model.Epic;
import model.Subtask;

import service.TaskManager;
import service.Managers;

public class Main {

    // Метод для разделительной строки
    private static void printDelimiter(){
        System.out.println();
        System.out.println("-".repeat(30));
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создание двух Задач
        System.out.println("Создаем задачи.");
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

        // Печать начального состояния списков Эпиков, Задач и Подзадач
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

        // Заполнение истории просмотра
        System.out.println("Запрашиваем задачи, чтобы заполнить историю.");

        // много запросов, чтобы превысить лимит 10
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(subtask3.getId());

        // Повторные запросы (дубли)
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());

        // Еще запросы, чтобы вытеснить старые и проверить, выполняется ли лимит в 10 задач
        manager.getTask(task2.getId());
        manager.getSubtask(subtask1.getId());

        // Проверка истории
        System.out.println("Проверка истории (10 элементов):");
        printAllTasks(manager);

        // Разделительная строка
        printDelimiter();

        // Удаление задачи и эпика
        System.out.println("Удаляем задачу 1 и эпик 1");
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("Состояние после удаления:");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }

        System.out.println();

        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }

        System.out.println();

        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println();

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
