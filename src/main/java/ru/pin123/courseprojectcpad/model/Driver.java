package ru.pin123.courseprojectcpad.model;

public class Driver {
    private Long driverId;
    private String lastName;
    private String firstName;
    private String middleName;

    private int age;
    private String passport;
    private byte[] driverImage;

    public byte[] getDriverImage() {
        return driverImage;
    }

    public void setDriverImage(byte[] driverImage) {
        this.driverImage = driverImage;
    }

    public Driver() {
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    // Переопределяем toString() для красивого отображения в списке выбора (например, при создании рейса)
    @Override
    public String toString() {
        return lastName + " " + firstName + (middleName != null && !middleName.isEmpty() ? " " + middleName : "");
    }
}