package service;

public class Managers {

    private Managers() {
    }

    // Реализация TaskManager
    public static TaskManager getDefault () {
        return new InMemoryTaskManager();
    }

    // Возвращение истории просмотров
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
