package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskTest {

    // Экземпляры класса Task равны друг другу, если равен их id
    @Test
    public void tasksShouldBeEqualIfIdEquals(){
        Task task1 = new Task("Task", "Description", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("Task", "Description", Status.NEW);
        task2.setId(1);

        Assertions.assertEquals(task1, task2, "Задачи должны быть равны, если равен их ID");
    }

}