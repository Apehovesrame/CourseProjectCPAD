package ru.pin123.courseprojectcpad.model;

public class User {
    private Long userId;
    private String login;
    private String lastName;
    private String firstName;
    private String middleName;

    // ДОБАВЛЕНО: Поле для связи пользователя с его ролью (Кассир/Админ)
    private Role role;

    public User() {
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

    // ДОБАВЛЕНО: Геттер и сеттер для роли
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}