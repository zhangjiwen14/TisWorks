package com.example.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mysql {
    public static Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://172.22.0.2:3306/test", "user", "password");
    }
}
