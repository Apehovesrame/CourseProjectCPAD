package ru.pin123.courseprojectcpad.model;

public class Stop {
    private Long stopId;
    private String name;

    public Stop(Long stopId, String name) {
        this.stopId = stopId;
        this.name = name;
    }

    public Stop() {
    }

    public Long getStopId() {
        return stopId;
    }

    public void setStopId(Long stopId) {
        this.stopId = stopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        // В интерфейсе остановка будет просто писаться своим названием (например, "Муром")
        return name;
    }
}