package com.unilib.database;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("=================================");
            System.out.println("MYSQL CONNECTION SUCCESSFUL!");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("MYSQL CONNECTION FAILED!");
            e.printStackTrace();
        }
    }
}
