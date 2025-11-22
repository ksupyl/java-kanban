package model;

import java.util.ArrayList; // импорт списка

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    // Конструктор
    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    // Геттер и сеттер
    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    // Очищение всех ID подзадач
    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    // Добавление ID подзадач
    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }
}
