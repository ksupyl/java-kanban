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

        // Создание Эпика с тремя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Эпик с 3 подзадачами");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Подзадача Эпика 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Подзадача Эпика 1", Status.NEW, epic1.getId());
        Subtask subtask3 = new Subtask("Подзадача 3", "Подзадача Эпика 1", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

        // Создание Эпика без подзадач
        Epic epic2 = new Epic("Эпик 2", "Эпик без подзадач");
        manager.createEpic(epic2);

        // Печать начального состояния списков Эпиков, Задач и Подзадач
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println();
        System.out.println("Эпики: " + manager.getEpics());
        System.out.println();
        System.out.println("Подзадачи: " + manager.getSubtasks());

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

        // Запрашивание задачи несколько раз в разном порядке
        System.out.println("Запрашиваем задачи в разном порядке.");
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task2.getId());
        manager.getSubtask(subtask2.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(subtask3.getId());

        // Повторные запросы = проверке, что дублей нет
        manager.getTask(task1.getId());       // если task1 уже был = перемещение в конец
        manager.getSubtask(subtask1.getId()); // если subtask1 уже был = перемещение в конец

        System.out.println("История (без повторов):");
        printAllTasks(manager);

        printDelimiter();

        // Удаление задачи из истории
        System.out.println("Удаляем задачу 1");
        manager.deleteTask(task1.getId());
        System.out.println("История после удаления задачи 1:");
        printAllTasks(manager);

        printDelimiter();

        // Удаление эпика с тремя подзадачами
        System.out.println("Удаляем эпик 1 (с тремя подзадачами)");
        manager.deleteEpic(epic1.getId());
        System.out.println("История после удаления эпика 1 и его подзадач:");
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
