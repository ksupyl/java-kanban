package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handler.EpicsHandler;
import http.handler.HistoryHandler;
import http.handler.PrioritizedHandler;
import http.handler.SubtasksHandler;
import http.handler.TasksHandler;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    // Порт, на котором будет работать сервер
    private static final int PORT = 8080;

    // Экземпляр HTTP-сервера из стандартной библиотеки Java
    private final HttpServer httpServer;

    // Менеджер задач, с которым будут работать обработчики запросов
    private final TaskManager taskManager;

    // Gson нужен для преобразования объектов Java в JSON и обратно
    private static final Gson GSON = new GsonBuilder().create();

    // Конструктор для обычного запуска приложения и для тестов
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики для каждого базового пути API
        httpServer.createContext("/tasks", new TasksHandler(this.taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(this.taskManager));
        httpServer.createContext("/epics", new EpicsHandler(this.taskManager));
        httpServer.createContext("/history", new HistoryHandler(this.taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(this.taskManager));
    }

    // Метод запуска сервера
    public void start() {
        httpServer.start();
        System.out.println("HTTP task server started on port " + PORT);
    }

    // Метод остановки сервера
    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP task server stopped on port " + PORT);
    }

    // Возвращает экземпляр Gson для работы с JSON
    public static Gson getGson() {
        return GSON;
    }

    // Точка входа в приложение
    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
        httpTaskServer.start();
    }
}