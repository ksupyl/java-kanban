package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    // Голова и хвост двусвязного списка
    private Node head;
    private Node tail;

    // В HashMap ключ = id задачи, значение = узел в списке
    private final HashMap<Integer, Node> historyMap = new HashMap<>();

    // Создание снимка задачи, чтобы история не зависела от внешних изменений
    private Task makeSnapshot(Task task) {
        if (task instanceof Subtask) {
            Subtask original = (Subtask) task;
            Subtask copy = new Subtask(
                    original.getName(),
                    original.getDescription(),
                    original.getStatus(),
                    original.getDuration(),
                    original.getStartTime(),
                    original.getEpicId()
            );
            copy.setId(original.getId());
            return copy;
        } else if (task instanceof Epic) {
            Epic original = (Epic) task;
            Epic copy = new Epic(original.getName(), original.getDescription());
            copy.setId(original.getId());
            copy.setStatus(original.getStatus());
            copy.setSubtaskIds(original.getSubtaskIds());
            copy.setEpicDuration(original.getDuration());
            copy.setEpicStartTime(original.getStartTime());
            copy.setEpicEndTime(original.getEndTime());
            return copy;
        } else {
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
    }

    // Добавление задачи в конец двусвязного списка
    private void linkLast(Task task) {
        Node newNode = new Node(makeSnapshot(task));
        if (tail == null) {
            // Если список пустой, то полностью новый узел - и голова, и хвост
            head = newNode;
        } else {
            // Иначе подвешивание к хвосту
            tail.setNext(newNode);
            newNode.setPrev(tail);
        }
        tail = newNode;

        // Сохранение узла в индексе
        historyMap.put(task.getId(), newNode);
    }

    // Удаление конкретного узла из списка
    private void removeNode(Node node) {
        if (node == null) return;

        Node prevNode = node.getPrev();
        Node nextNode = node.getNext();

        if (prevNode != null) {
            prevNode.setNext(nextNode);  // левый сосед смотрит на правого
        } else {
            head = nextNode;           // удаление головы и новая голова - это следующий
        }

        if (nextNode != null) {
            nextNode.setPrev(prevNode);  // правый сосед теперь смотрит на левого
        } else {
            tail = prevNode;           // удаление хвоста и новый хвост - это предыдущий
        }

        // Обнуление ссылок удалённого узла (помощь Garbage Collector)
        node.setPrev(null);
        node.setNext(null);
    }

    // Сбор задач всех из двусвязного списка в ArrayList
    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.getTask());
            current = current.getNext();
        }
        return result;
    }

    // Добавление в историю
    @Override
    public void add(Task task) {
        if (task == null) return;

        // Если задача уже в истории = удаление старого просмотра
        Node existingNode = historyMap.remove(task.getId());
        if (existingNode != null) {
            removeNode(existingNode);
        }

        // Добавление копии в конец списка
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
