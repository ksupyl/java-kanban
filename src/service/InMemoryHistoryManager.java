package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_LIMIT = 10;

    // Список для хранения истории просмотров задач
    private final List<Task> history = new ArrayList<>();

    // Добавление в историю
    @Override
    public void add(Task task) {
        history.add(task);
        // Ограничение в 10 элементов
        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }
    }

    // Возвращение списка истории
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
