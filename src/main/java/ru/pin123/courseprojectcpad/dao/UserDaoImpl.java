package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Role; // ИСПРАВЛЕНО: Теперь используется ваша модель
import ru.pin123.courseprojectcpad.model.User;
import ru.pin123.courseprojectcpad.util.HashHelper; // ИСПРАВЛЕНО: Добавлен утилитарный класс

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // ИСПРАВЛЕНО: Добавлен Statement
import java.util.ArrayList; // ИСПРАВЛЕНО: Добавлен ArrayList
import java.util.List;
import java.util.Optional;

public class UserDaoImpl {

    public Optional<User> authenticate(String login, String passwordHash) {
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
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при авторизации", e);
        }

        return Optional.empty();
    }

    public List<Role> findAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getLong("role_id"));
                role.setRoleName(rs.getString("role_name"));
                roles.add(role);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки ролей", e);
        }
        return roles;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id ORDER BY u.last_name";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setLogin(rs.getString("login"));
                user.setLastName(rs.getString("last_name"));
                user.setFirstName(rs.getString("first_name"));

                Role role = new Role();
                role.setRoleId(rs.getLong("role_id"));
                role.setRoleName(rs.getString("role_name"));
                user.setRole(role);

                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки пользователей", e);
        }
        return users;
    }

    // ИСПРАВЛЕНО: Транзакция для вставки в 2 таблицы (authorizations и users)
    public void save(User user, String rawPassword) {
        String sqlAuth = "INSERT INTO authorizations (login, password_hash) VALUES (?, ?)";
        String sqlUser = "INSERT INTO users (login, last_name, first_name, role_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false); // Открываем транзакцию

            try (PreparedStatement pstmtAuth = conn.prepareStatement(sqlAuth);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {

                // 1. Вставка логина и хеша
                pstmtAuth.setString(1, user.getLogin());
                pstmtAuth.setString(2, HashHelper.computeSha256Hash(rawPassword));
                pstmtAuth.executeUpdate();

                // 2. Вставка данных профиля
                pstmtUser.setString(1, user.getLogin());
                pstmtUser.setString(2, user.getLastName());
                pstmtUser.setString(3, user.getFirstName());
                pstmtUser.setLong(4, user.getRole().getRoleId());
                pstmtUser.executeUpdate();

                conn.commit(); // Если обе вставки прошли без ошибок — сохраняем!
            } catch (SQLException e) {
                conn.rollback(); // Если ошибка (например, логин занят) — откатываем
                throw new RuntimeException("Ошибка сохранения. Возможно, такой логин уже существует.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    // ИСПРАВЛЕНО: Удаляем профиль, а затем учетную запись для входа (каскадное удаление)
    public void delete(Long userId) {
        String sqlSelectLogin = "SELECT login FROM users WHERE user_id = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE user_id = ?";
        String sqlDeleteAuth = "DELETE FROM authorizations WHERE login = ?";

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelectLogin);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlDeleteUser);
                 PreparedStatement pstmtAuth = conn.prepareStatement(sqlDeleteAuth)) {

                // 1. Сначала узнаем логин пользователя по его ID
                pstmtSelect.setLong(1, userId);
                String loginToDelete = null;
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        loginToDelete = rs.getString("login");
                    }
                }

                // 2. Если пользователь найден, удаляем его из обеих таблиц
                if (loginToDelete != null) {
                    pstmtUser.setLong(1, userId);
                    pstmtUser.executeUpdate();

                    pstmtAuth.setString(1, loginToDelete);
                    pstmtAuth.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Невозможно удалить пользователя. Убедитесь, что на него не завязаны рейсы.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к БД при удалении", e);
        }
    }
}