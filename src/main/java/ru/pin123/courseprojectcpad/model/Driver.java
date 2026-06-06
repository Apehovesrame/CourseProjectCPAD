package ru.pin123.courseprojectcpad.model;

public class Driver {
    private Long driverId;
    private String lastName;
    private String firstName;
    private String middleName;

    // Новые поля
    private int age;
    private String passport;
    private String photoPath;

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

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // Переопределяем toString() для красивого отображения в списке выбора (например, при создании рейса)
    @Override
    public String toString() {
        return lastName + " " + firstName + (middleName != null && !middleName.isEmpty() ? " " + middleName : "");
    }
}