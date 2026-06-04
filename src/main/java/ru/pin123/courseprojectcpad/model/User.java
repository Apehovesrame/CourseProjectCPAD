package ru.pin123.courseprojectcpad.model;

public class User {
    private Long userId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String login;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}