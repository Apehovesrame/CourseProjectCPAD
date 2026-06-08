package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Role;
import ru.pin123.courseprojectcpad.model.User;
import ru.pin123.courseprojectcpad.util.HashHelper;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl {

    public Optional<User> authenticate(String login, String passwordHash) {
        String sql = PropertiesUtil.get("sql.user.authenticate");

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

                    // ИСПРАВЛЕНО: Теперь роль корректно заполняется при входе!
                    Role role = new Role();
                    role.setRoleId(rs.getLong("role_id"));
                    role.setRoleName(rs.getString("role_name"));
                    user.setRole(role);

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
        String sql = PropertiesUtil.get("sql.user.find_all_roles");
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
        String sql = PropertiesUtil.get("sql.user.find_all");
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setLogin(rs.getString("login"));
                user.setLastName(rs.getString("last_name"));
                user.setFirstName(rs.getString("first_name"));
                user.setMiddleName(rs.getString("middle_name"));

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

    public void save(User user, String rawPassword) {
        String sqlAuth = PropertiesUtil.get("sql.user.insert_auth");
        String sqlUser = PropertiesUtil.get("sql.user.insert_user");

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtAuth = conn.prepareStatement(sqlAuth);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {

                pstmtAuth.setString(1, user.getLogin());
                pstmtAuth.setString(2, HashHelper.computeSha256Hash(rawPassword));
                pstmtAuth.executeUpdate();

                pstmtUser.setString(1, user.getLogin());
                pstmtUser.setString(2, user.getLastName());
                pstmtUser.setString(3, user.getFirstName());
                pstmtUser.setString(4, user.getMiddleName()); // ИСПРАВЛЕНО: Передаем отчество
                pstmtUser.setLong(5, user.getRole().getRoleId());
                pstmtUser.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Ошибка сохранения. Возможно, такой логин уже существует.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    public void delete(Long userId) {
        String sqlSelectLogin = PropertiesUtil.get("sql.user.find_login_by_id");
        String sqlDeleteUser = PropertiesUtil.get("sql.user.delete_user");
        String sqlDeleteAuth = PropertiesUtil.get("sql.user.delete_auth");

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelectLogin);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlDeleteUser);
                 PreparedStatement pstmtAuth = conn.prepareStatement(sqlDeleteAuth)) {

                pstmtSelect.setLong(1, userId);
                String loginToDelete = null;
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        loginToDelete = rs.getString("login");
                    }
                }

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