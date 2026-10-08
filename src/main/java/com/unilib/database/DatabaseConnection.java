package com.unilib.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String HOST =
            System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String PORT =
            System.getenv().getOrDefault("DB_PORT", "3306");

    private static final String DATABASE =
            System.getenv().getOrDefault("DB_NAME", "unilib");

    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "unilib_app");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "");

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}