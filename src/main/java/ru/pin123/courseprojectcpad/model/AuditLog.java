package ru.pin123.courseprojectcpad.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private Long logId;
    private String tableName;
    private String actionType;
    private String oldData;
    private String newData;
    private String dbUser;
    private LocalDateTime changedAt;

    public AuditLog(Long logId, String tableName, String actionType, String oldData, String newData, String dbUser, LocalDateTime changedAt) {
        this.logId = logId;
        this.tableName = tableName;
        this.actionType = actionType;
        this.oldData = oldData;
        this.newData = newData;
        this.dbUser = dbUser;
        this.changedAt = changedAt;
    }

    // Геттеры
    public Long getLogId() { return logId; }
    public String getTableName() { return tableName; }
    public String getActionType() { return actionType; }
    public String getOldData() { return oldData; }
    public String getNewData() { return newData; }
    public String getDbUser() { return dbUser; }
    public LocalDateTime getChangedAt() { return changedAt; }

    // Удобный форматтер для вывода даты в таблице
    public String getFormattedDate() {
        if (changedAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return changedAt.format(formatter);
    }
}