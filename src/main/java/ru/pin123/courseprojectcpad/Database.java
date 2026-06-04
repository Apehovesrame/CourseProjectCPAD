//package ru.pin123.lab9cpad;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class Database {
//    private static final String URL = "jdbc:postgresql://localhost:5432/passenger_transport";
//    private static final String USER = "postgres";
//    private static final String PASSWORD = "1234";
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }
//}