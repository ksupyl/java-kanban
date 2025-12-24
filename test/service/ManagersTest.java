package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagersTest {

    // Утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        Assertions.assertNotNull(taskManager, "Утилитарный класс должен возвращать проинициализированный экземпляр TaskManager");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        Assertions.assertNotNull(historyManager, "Утилитарный класс должен возвращать проинициализированный экземпляр HistoryManager");
    }

}