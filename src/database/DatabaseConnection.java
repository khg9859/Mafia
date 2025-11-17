package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // ✅ Mac 환경에서도 호환 가능한 JDBC URL (SSL 비활성 + 인코딩 명시)
    private static final String URL = "jdbc:mysql://localhost:3306/mafia?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // 비밀번호 없음

    private static Connection conn = null;

    // ✅ 클래스 로드 시점에 드라이버 로드
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ JDBC 드라이버 로드 성공 (com.mysql.cj.jdbc.Driver)");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC 드라이버 로드 실패: " + e.getMessage());
        }
    }

    // ✅ DB 연결 반환 메서드
    public static Connection getConnection() {
        try {
            // 이미 연결되어 있으면 재사용
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ DB 연결 성공");
            }
        } catch (SQLException e) {
            System.out.println("❌ DB 연결 실패: " + e.getMessage());
            conn = null;
        }
        return conn;
    }

    // ✅ 연결 종료 메서드 (선택적)
    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔒 DB 연결 종료");
            }
        } catch (SQLException e) {
            System.out.println("⚠️ DB 연결 종료 중 오류: " + e.getMessage());
        }
    }
}