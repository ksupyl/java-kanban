package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    // Наследники класса (Epic) Task равны друг другу, если равен их id
    @Test
    public void epicsShouldBeEqualIfIdEquals(){
        Epic epic1 = new Epic("Epic", "Description");
        epic1.setId(2);

        Epic epic2 = new Epic("Epic", "Description");
        epic2.setId(2);

        Assertions.assertEquals(epic1, epic2, "Эпики должны быть равны, если равен их ID");
    }

    // Объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    public void epicCannotAddItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        epic.addSubtaskId(epic.getId());

        Assertions.assertEquals(0, epic.getSubtaskIds().size(), "Эпик не должен добавлять сам себя в подзадачи");
    }

}