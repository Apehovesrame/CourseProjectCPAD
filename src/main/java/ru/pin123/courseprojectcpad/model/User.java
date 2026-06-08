package ru.pin123.courseprojectcpad.model;

public class User {
    private Long userId;
    private String login;
    private String passwordHash; // ДОБАВИЛИ ПОЛЕ ДЛЯ ХЕША
    private String lastName;
    private String firstName;
    private String middleName;
    private Role role;

    public User() {
    }

    // Дополнительный удобный геттер для полного имени (пригодится для вывода в UI)
    public String getFullName() {
        return String.format("%s %s %s",
                lastName != null ? lastName : "",
                firstName != null ? firstName : "",
                middleName != null ? middleName : "").trim().replaceAll("\\s+", " ");
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() { // ГЕТТЕР
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}