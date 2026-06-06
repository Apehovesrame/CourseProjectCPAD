package ru.pin123.courseprojectcpad.model;

public class Bus {
    private Long busId;
    private String licensePlate;
    private String model;
    private int seatCapacity;
    private String photoPath;

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public Bus() {
    }

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

    @Override
    public String toString() {
        // Так автобус будет красиво отображаться в ComboBox
        return model + " (" + licensePlate + ") - " + seatCapacity + " мест";
    }
}