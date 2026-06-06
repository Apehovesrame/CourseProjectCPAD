package ru.pin123.courseprojectcpad.model;

public class Passenger {
    private Long passengerId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String passportNumber;
    private int birthYear;

    public Passenger(Long passengerId, String lastName, String firstName, String middleName, String passportNumber) {
        this.passengerId = passengerId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.passportNumber = passportNumber;
    }

    public Passenger() {
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
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

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }
}