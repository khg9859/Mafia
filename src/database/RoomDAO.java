package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    // 방 정보를 담는 내부 클래스
    public static class Room {
        public int roomId;
        public String roomName;
        public int maxPlayers;
        public int currentPlayers;
        public String gameStatus;
        public int createdBy;
        public Timestamp createdAt;

        public Room(int roomId, String roomName, int maxPlayers, int currentPlayers,
                   String gameStatus, int createdBy, Timestamp createdAt) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.maxPlayers = maxPlayers;
            this.currentPlayers = currentPlayers;
            this.gameStatus = gameStatus;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
        }
    }

    // 모든 방 목록 조회
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Room room = new Room(
                    rs.getInt("room_id"),
                    rs.getString("room_name"),
                    rs.getInt("max_players"),
                    rs.getInt("current_players"),
                    rs.getString("game_status"),
                    rs.getInt("created_by"),
                    rs.getTimestamp("created_at")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("❌ 방 목록 조회 실패: " + e.getMessage());
        }
        return rooms;
    }

    // 특정 방 정보 조회
    public static Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Room(
                    rs.getInt("room_id"),
                    rs.getString("room_name"),
                    rs.getInt("max_players"),
                    rs.getInt("current_players"),
                    rs.getString("game_status"),
                    rs.getInt("created_by"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ 방 정보 조회 실패: " + e.getMessage());
        }
        return null;
    }

    // 새 방 생성
    public static int createRoom(String roomName, int maxPlayers, int createdBy) {
        String sql = "INSERT INTO rooms (room_name, max_players, created_by) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, roomName);
            pstmt.setInt(2, maxPlayers);
            pstmt.setInt(3, createdBy);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int roomId = rs.getInt(1);
                    System.out.println("✅ 방 생성 성공: " + roomName + " (ID: " + roomId + ")");
                    return roomId;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 방 생성 실패: " + e.getMessage());
        }
        return -1;
    }

    // 플레이어 방 입장
    public static boolean joinRoom(int roomId, int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. 방 정보 조회 (락 걸기)
            String checkSql = "SELECT current_players, max_players FROM rooms WHERE room_id = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, roomId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return false;
            }

            int current = rs.getInt("current_players");
            int max = rs.getInt("max_players");

            if (current >= max) {
                System.out.println("❌ 방이 가득 찼습니다.");
                conn.rollback();
                return false;
            }

            // 2. room_players에 추가
            String insertSql = "INSERT INTO room_players (room_id, user_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, roomId);
            insertStmt.setInt(2, userId);
            insertStmt.executeUpdate();

            // 3. current_players 증가
            String updateSql = "UPDATE rooms SET current_players = current_players + 1 WHERE room_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, roomId);
            updateStmt.executeUpdate();

            conn.commit();
            System.out.println("✅ 방 입장 성공: Room " + roomId + ", User " + userId);
            return true;

        } catch (SQLException e) {
            System.out.println("❌ 방 입장 실패: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 플레이어 방 퇴장
    public static boolean leaveRoom(int roomId, int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. room_players에서 삭제
            String deleteSql = "DELETE FROM room_players WHERE room_id = ? AND user_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, roomId);
            deleteStmt.setInt(2, userId);
            int deleted = deleteStmt.executeUpdate();

            if (deleted == 0) {
                conn.rollback();
                return false;
            }

            // 2. current_players 감소
            String updateSql = "UPDATE rooms SET current_players = current_players - 1 WHERE room_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, roomId);
            updateStmt.executeUpdate();

            conn.commit();
            System.out.println("✅ 방 퇴장 성공: Room " + roomId + ", User " + userId);
            return true;

        } catch (SQLException e) {
            System.out.println("❌ 방 퇴장 실패: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 방에 있는 플레이어 목록 조회
    public static List<String> getPlayersInRoom(int roomId) {
        List<String> players = new ArrayList<>();
        String sql = "SELECT u.nickname FROM room_players rp " +
                    "JOIN user u ON rp.user_id = u.user_id " +
                    "WHERE rp.room_id = ? ORDER BY rp.joined_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                players.add(rs.getString("nickname"));
            }
        } catch (SQLException e) {
            System.out.println("❌ 플레이어 목록 조회 실패: " + e.getMessage());
        }
        return players;
    }
}
