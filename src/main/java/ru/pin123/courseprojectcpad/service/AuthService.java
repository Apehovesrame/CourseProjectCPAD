package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.UserDaoImpl;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.model.User;

import java.util.Optional;

public class AuthService {

    private final UserDaoImpl userDao = new UserDaoImpl();

    public void login(String login, String rawPassword) {
        if (login == null || login.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин и пароль не могут быть пустыми.");
        }

        // 1. Хешируем введенный пароль
        String hash = HashUtil.computeSha256Hash(rawPassword);

        // 2. Ищем в базе
        Optional<User> userOpt = userDao.authenticate(login, hash);

        // 3. Обрабатываем результат
        if (userOpt.isPresent()) {
            // Если пароль верный, сохраняем пользователя в глобальную сессию
            Session.setCurrentUser(userOpt.get());
        } else {
            throw new RuntimeException("Неверный логин или пароль!");
        }
    }
}