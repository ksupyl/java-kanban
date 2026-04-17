package service.exception;

// Исключение, которое выбрасывается, если объект не найден.
// Используется в HTTP-обработчиках, чтобы вернуть статус 404.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
