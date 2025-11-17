package server;

import database.RoomDAO;
import protocol.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 테스트용 마피아 게임 서버
 * - 포트 9998에서 실행 (기본 서버와 동시 실행 가능)
 * - 2명만으로 게임 시작 가능
 * - 한 명이 게임 시작하면 모든 플레이어가 동시에 시작
 */
public class TestMafiaServer {
    private static final int PORT = 9998;
    private static final int MIN_PLAYERS = 2; // 테스트용: 최소 2명
    
    private ServerSocket serverSocket;
    private List<TestClientHandler> clients = new CopyOnWriteArrayList<>();
    private TestRoomManager roomManager;
    private boolean running = false;

    public TestMafiaServer() {
        roomManager = new TestRoomManager(this);
    }

    /**
     * 서버 시작
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("========================================");
            System.out.println("🧪 마피아42 테스트 서버 시작");
            System.out.println("📡 포트: " + PORT);
            System.out.println("👥 최소 인원: " + MIN_PLAYERS + "명");
            System.out.println("⏰ 시작 시각: " + new java.util.Date());
            System.out.println("========================================");

            // 클라이언트 연결 수락 루프
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    TestClientHandler handler = new TestClientHandler(clientSocket, this, roomManager);
                    clients.add(handler);
                    handler.start();
                    System.out.println("✅ 새 연결 수락 (현재 접속자: " + clients.size() + "명)");
                } catch (IOException e) {
                    if (running) {
                        System.out.println("⚠️ 클라이언트 연결 수락 오류: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("❌ 서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 서버 종료
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (TestClientHandler client : clients) {
                client.sendMessage(Message.systemMessage("서버가 종료됩니다."));
            }
            System.out.println("🔒 테스트 서버 종료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 클라이언트 제거
     */
    public void removeClient(TestClientHandler client) {
        clients.remove(client);
        System.out.println("📤 클라이언트 제거 (현재 접속자: " + clients.size() + "명)");
    }

    /**
     * 방 목록 데이터를 문자열로 반환 (테스트용: 최대 인원 2명으로 표시)
     */
    public String getRoomListData() {
        List<RoomDAO.Room> rooms = RoomDAO.getAllRooms();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rooms.size(); i++) {
            RoomDAO.Room room = rooms.get(i);
            sb.append(room.roomId).append("|")
              .append(room.roomName).append(" [테스트]|")
              .append(room.currentPlayers).append("/2|") // 테스트용: 2명으로 표시
              .append(room.gameStatus);

            if (i < rooms.size() - 1) {
                sb.append(";");
            }
        }

        return sb.toString();
    }

    /**
     * 최소 플레이어 수 반환
     */
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }

    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        TestMafiaServer server = new TestMafiaServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⚠️ 테스트 서버 종료 신호 감지...");
            server.stop();
        }));

        server.start();
    }
}
