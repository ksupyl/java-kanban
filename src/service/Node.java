package service;

import model.Task;

// Двусвязный список с самой задачей и ссылками на соседей
public class Node {
    Task task;
    Node prev;
    Node next;

    public Node(Task task) {
        this.task = task;
    }
}
