@Override
public String toString() {
    // Формируем красивое ФИО: Иванов Иван Иванович (или без отчества, если его нет)
    StringBuilder fullName = new StringBuilder(lastName).append(" ").append(firstName);
    if (middleName != null && !middleName.trim().isEmpty()) {
        fullName.append(" ").append(middleName);
    }
    return fullName.toString();
}