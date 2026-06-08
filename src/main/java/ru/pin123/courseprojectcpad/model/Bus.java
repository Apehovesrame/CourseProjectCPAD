package ru.pin123.courseprojectcpad.model;

public class Bus {
    private Long busId;
    private String licensePlate;
    private String model;
    private int seatCapacity;
    private byte[] busImage; // Поле для хранения бинарных данных (картинки) из bytea

    // Пустой конструктор (нужен для некоторых библиотек/фреймворков)
    public Bus() {
    }

    // Удобный конструктор для создания объектов (например, при чтении из БД)
    public Bus(Long busId, String model, String licensePlate, int seatCapacity, byte[] busImage) {
        this.busId = busId;
        this.model = model;
        this.licensePlate = licensePlate;
        this.seatCapacity = seatCapacity;
        this.busImage = busImage;
    }

    // Геттеры и Сеттеры
    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public byte[] getBusImage() {
        return busImage;
    }

    public void setBusImage(byte[] busImage) {
        this.busImage = busImage;
    }

    @Override
    public String toString() {
        // Так автобус будет красиво отображаться в ComboBox
        return model + " (" + licensePlate + ") - " + seatCapacity + " мест";
    }
}