package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = new ArrayList<>(subtaskIds);
    }

    // Очищение всех ID подзадач
    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    // Добавление ID подзадач
    public void addSubtaskId(int subtaskId) {
        if (subtaskId != this.getId()) {
            subtaskIds.add(subtaskId);
        }
    }

    // Удаление определенного ID подзадач
    public void removeSubtaskId(Integer subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public void setEpicDuration(Duration duration) {
        setDuration(duration);
    }

    public void setEpicStartTime(LocalDateTime startTime) {
        setStartTime(startTime);
    }

    public void setEpicEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{"
                + "id=" + getId()
                + ", name='" + getName() + '\''
                + ", description='" + getDescription() + '\''
                + ", status=" + getStatus()
                + ", duration=" + getDuration()
                + ", startTime=" + getStartTime()
                + ", endTime=" + getEndTime()
                + ", subtaskIds=" + subtaskIds
                + '}';
    }
}