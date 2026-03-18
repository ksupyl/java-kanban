package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    // Наследники класса (Subtask) Task равны друг другу, если равен их id
    @Test
    public void subtasksShouldBeEqualIfIdEquals(){
        Subtask subtask1 = new Subtask("Subtask1", "Description1", Status.NEW, 1);
        subtask1.setId(3);

        Subtask subtask2 = new Subtask("Subtask2", "Description2", Status.NEW, 1);
        subtask2.setId(3);

        Assertions.assertEquals(subtask1, subtask2, "Подзадачи должны быть равны, если равен их ID");
    }

    // Объект Subtask нельзя сделать своим же эпиком
    @Test
    public void subtaskCannotBeEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 3);

        subtask.setId(5);
        Assertions.assertEquals(5, subtask.getId(), "Корректный id должен установиться");

        subtask.setId(3);
        Assertions.assertEquals(5, subtask.getId(),
                "Подзадача не должна позволять устанавливать ID, равный EpicID");
    }
}