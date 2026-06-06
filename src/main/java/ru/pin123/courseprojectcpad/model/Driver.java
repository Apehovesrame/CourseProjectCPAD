package ru.pin123.courseprojectcpad.model;

public class Driver {
    private Long driverId;
    private String lastName;
    private String firstName;
    private String middleName;

    // Пустой конструктор
    public Driver() {}

    // Конструктор (если понадобится для создания объекта)
    public Driver(Long driverId, String lastName, String firstName, String middleName) {
        this.driverId = driverId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ (именно они нужны вашему DAO) ---

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

    // --- Ваш метод toString() ---
    @Override
    public String toString() {
        String l = (lastName == null) ? "" : lastName;
        String f = (firstName == null) ? "" : firstName;
        String m = (middleName == null) ? "" : middleName;

        StringBuilder fullName = new StringBuilder();

        if (!l.isEmpty()) fullName.append(l);
        if (!f.isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(f);
        }
        if (!m.isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(m);
        }

        return fullName.length() > 0 ? fullName.toString() : "Водитель ID: " + driverId;
    }
}