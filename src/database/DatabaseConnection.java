package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/mafia?serverTimezone=Asia/Seoul";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    private static Connection conn;

    // ✅ 드라이버를 클래스 로드 시점에 무조건 등록
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ JDBC 드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC 드라이버 로드 실패: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ DB 연결 성공");
        } catch (SQLException e) {
            System.out.println("❌ DB 연결 실패: " + e.getMessage());
            conn = null;
        }
        return conn;
    }
}