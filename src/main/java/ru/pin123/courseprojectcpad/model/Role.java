package ru.pin123.courseprojectcpad.model;

public class Role {
    private Long roleId;
    private String roleName;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    @Override
    public String toString() {
        return roleName; // Для красивого отображения в ComboBox при создании пользователя
    }
}