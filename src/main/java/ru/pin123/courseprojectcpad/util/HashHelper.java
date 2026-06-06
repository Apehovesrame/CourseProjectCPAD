package ru.pin123.courseprojectcpad.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {
    public static String computeSha256Hash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Ошибка: алгоритм SHA-256 не найден", ex);
        }
    }
}