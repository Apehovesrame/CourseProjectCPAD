package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDaoImpl {

    public Optional<User> authenticate(String login, String passwordHash) {
        // Запрашиваем данные из двух таблиц сразу
        String sql = "SELECT u.user_id, u.role_id, u.login, u.last_name, u.first_name, u.middle_name " +
                "FROM authorizations a " +
                "JOIN users u ON a.login = u.login " +
                "WHERE a.login = ? AND a.password_hash = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setLogin(rs.getString("login"));
                    user.setLastName(rs.getString("last_name"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setMiddleName(rs.getString("middle_name"));
                    // Если у вас в модели User есть поле roleId, можно добавить:
                    // user.setRoleId(rs.getLong("role_id"));

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при авторизации", e);
        }

        return Optional.empty(); // Если ничего не найдено
    }
}