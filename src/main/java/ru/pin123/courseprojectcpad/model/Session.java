package ru.pin123.courseprojectcpad.model;

public class Session {
    // Статическое поле, доступное из любой точки приложения
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}