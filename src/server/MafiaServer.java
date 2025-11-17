package server;

import database.RoomDAO;
import protocol.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 마피아 게임 서버
 * - 포트 9999에서 클라이언트 연결 대기
 * - 멀티스레드로 여러 클라이언트 동시 처리
 * - 방별 채팅 메시지 브로드캐스트
 */
public class MafiaServer {
    private static final int PORT = 9999;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private RoomManager roomManager;
    private boolean running = false;

    public MafiaServer() {
        roomManager = new RoomManager();
    }

    /**
     * 서버 시작
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("========================================");
            System.out.println("🎮 마피아42 서버 시작");
            System.out.println("📡 포트: " + PORT);
            System.out.println("⏰ 시작 시각: " + new java.util.Date());
            System.out.println("========================================");

            // 클라이언트 연결 수락 루프
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this, roomManager);
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
            // 모든 클라이언트 연결 종료
            for (ClientHandler client : clients) {
                client.sendMessage(Message.systemMessage("서버가 종료됩니다."));
            }
            System.out.println("🔒 서버 종료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 클라이언트 제거
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("📤 클라이언트 제거 (현재 접속자: " + clients.size() + "명)");
    }

    /**
     * 방 목록 데이터를 문자열로 반환
     * 형식: roomId|roomName|current/max|status;roomId|roomName|current/max|status;...
     */
    public String getRoomListData() {
        List<RoomDAO.Room> rooms = RoomDAO.getAllRooms();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rooms.size(); i++) {
            RoomDAO.Room room = rooms.get(i);
            sb.append(room.roomId).append("|")
              .append(room.roomName).append("|")
              .append(room.currentPlayers).append("/").append(room.maxPlayers).append("|")
              .append(room.gameStatus);

            if (i < rooms.size() - 1) {
                sb.append(";");
            }
        }

        return sb.toString();
    }

    /**
     * 모든 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcastToAll(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * 서버 통계 출력
     */
    public void printStats() {
        System.out.println("\n========== 서버 상태 ==========");
        System.out.println("접속자 수: " + clients.size());
        System.out.println("==============================\n");
    }

    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        MafiaServer server = new MafiaServer();

        // Shutdown hook 등록 (Ctrl+C로 종료 시 정리)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⚠️ 서버 종료 신호 감지...");
            server.stop();
        }));

        // 서버 시작
        server.start();
    }
}
