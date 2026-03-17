package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    // Двусвязный список с самой задачей и ссылками на соседей
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    // Голова и хвост двусвязного списка
    private Node head;
    private Node tail;

    // В HashMap ключ = id задачи, значение = узел в списке
    private final HashMap<Integer, Node> historyMap = new HashMap<>();

    // Добавление задачи в конец двусвязного списка
    private void linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            // Если список пустой, то полностью новый узел - и голова, и хвост
            head = newNode;
            tail = newNode;
        } else {
            // Иначе подвешивание к хвосту
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        // Сохранение узла в индексе
        historyMap.put(task.getId(), newNode);
    }

    // Удаление конкретного узла из списка
    private void removeNode(Node node) {
        if (node == null) return;

        Node prevNode = node.prev;
        Node nextNode = node.next;

        if (prevNode != null) {
            prevNode.next = nextNode;  // левый сосед смотрит на правого
        } else {
            head = nextNode;           // удаление головы и новая голова - это следующий
        }

        if (nextNode != null) {
            nextNode.prev = prevNode;  // правый сосед теперь смотрит на левого
        } else {
            tail = prevNode;           // удаление хвоста и новый хвост - это предыдущий
        }

        // Обнуление ссылок удалённого узла (помощь Garbage Collector)
        node.prev = null;
        node.next = null;
    }

    // Сбор задач всех из двусвязного списка в ArrayList
    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    // Добавление в историю
    @Override
    public void add(Task task) {
        if (task == null) return;

        // Если задача уже в истории, значит удаление старого просмотра
        if (historyMap.containsKey(task.getId())) {
            removeNode(historyMap.get(task.getId()));
        }

        // Добавление в конец списка и обновление индекса
        linkLast(task);
    }

    // Удаление задачи из истории по id
    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);
        removeNode(node);
    }

    // Возвращение списка истории
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
