package server;

import protocol.Message;

import java.io.*;
import java.net.Socket;

/**
 * 테스트 서버용 클라이언트 핸들러
 * - 게임 시작 메시지 처리 추가
 */
public class TestClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private TestMafiaServer server;
    private TestRoomManager roomManager;

    private String username;
    private String nickname;
    private int userId;
    private Integer currentRoomId = null;

    public TestClientHandler(Socket socket, TestMafiaServer server, TestRoomManager roomManager) {
        this.socket = socket;
        this.server = server;
        this.roomManager = roomManager;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            System.out.println("✅ 새 클라이언트 연결: " + socket.getInetAddress());

            String line;
            while ((line = in.readLine()) != null) {
                Message msg = Message.deserialize(line);
                if (msg != null) {
                    handleMessage(msg);
                }
            }

        } catch (IOException e) {
            System.out.println("⚠️ 클라이언트 연결 종료: " + nickname);
        } finally {
            cleanup();
        }
    }

    /**
     * 메시지 처리
     */
    private void handleMessage(Message msg) {
        System.out.println("📩 수신 [" + nickname + "]: " + msg);

        switch (msg.getType()) {
            case LOGIN:
                handleLogin(msg);
                break;

            case ROOM_LIST_REQUEST:
                handleRoomListRequest();
                break;

            case ROOM_JOIN:
                handleRoomJoin(msg);
                break;

            case ROOM_LEAVE:
                handleRoomLeave();
                break;

            case CHAT_MESSAGE:
                handleChatMessage(msg);
                break;

            case GAME_START:
                handleGameStart();
                break;

            case DISCONNECT:
                cleanup();
                break;

            default:
                System.out.println("⚠️ 처리되지 않은 메시지 타입: " + msg.getType());
        }
    }

    /**
     * 로그인 처리
     */
    private void handleLogin(Message msg) {
        String[] parts = msg.getData().split("\\|");
        if (parts.length >= 2) {
            this.username = parts[0];
            this.nickname = parts[1];
            if (parts.length >= 3) {
                this.userId = Integer.parseInt(parts[2]);
            }

            sendMessage(Message.loginSuccess());
            System.out.println("✅ 로그인 성공: " + nickname + " (User ID: " + userId + ")");
        } else {
            sendMessage(Message.loginFailed("잘못된 로그인 데이터"));
        }
    }

    /**
     * 방 목록 요청 처리
     */
    private void handleRoomListRequest() {
        String roomListData = server.getRoomListData();
        sendMessage(Message.roomListResponse(roomListData));
    }

    /**
     * 방 입장 처리 (테스트용: 2명까지만 입장 가능)
     */
    private void handleRoomJoin(Message msg) {
        try {
            int roomId = Integer.parseInt(msg.getData());

            // 현재 방 인원 확인
            int currentCount = roomManager.getClientCountInRoom(roomId);
            if (currentCount >= 2) {
                sendMessage(Message.roomJoinFailed("테스트 모드: 방이 가득 찼습니다 (최대 2명)"));
                return;
            }

            // DB에 방 입장 기록
            if (database.RoomDAO.joinRoom(roomId, userId)) {
                currentRoomId = roomId;
                roomManager.addClientToRoom(roomId, this);

                // 입장 성공 알림
                database.RoomDAO.Room room = database.RoomDAO.getRoomById(roomId);
                sendMessage(Message.roomJoinSuccess(roomId, room.roomName + " [테스트]"));

                // 방의 플레이어 목록 전송
                String playerList = roomManager.getPlayerListString(roomId);
                sendMessage(Message.playerList(playerList));

                // 다른 플레이어들에게 새 플레이어 입장 알림
                roomManager.broadcastToRoomExcept(roomId,
                    Message.playerJoined(nickname), this);

                System.out.println("✅ " + nickname + " -> Room " + roomId + " 입장 (현재 인원: " + (currentCount + 1) + "/2)");
            } else {
                sendMessage(Message.roomJoinFailed("방 입장에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendMessage(Message.error("잘못된 방 ID"));
        }
    }

    /**
     * 방 퇴장 처리
     */
    private void handleRoomLeave() {
        if (currentRoomId != null) {
            database.RoomDAO.leaveRoom(currentRoomId, userId);
            roomManager.broadcastToRoom(currentRoomId, Message.playerLeft(nickname));
            roomManager.removeClientFromRoom(currentRoomId, this);

            System.out.println("✅ " + nickname + " <- Room " + currentRoomId + " 퇴장");
            currentRoomId = null;
        }
    }

    /**
     * 채팅 메시지 처리
     */
    private void handleChatMessage(Message msg) {
        if (currentRoomId != null) {
            Message chatMsg = Message.chatMessage(nickname, msg.getData());
            roomManager.broadcastToRoom(currentRoomId, chatMsg);
            System.out.println("💬 [Room " + currentRoomId + "] " + nickname + ": " + msg.getData());
        }
    }

    /**
     * 게임 시작 처리 (테스트용)
     */
    private void handleGameStart() {
        if (currentRoomId != null) {
            System.out.println("🎮 게임 시작 요청 from " + nickname + " in Room " + currentRoomId);
            roomManager.handleGameStart(currentRoomId, this);
        } else {
            sendMessage(Message.error("방에 입장하지 않았습니다."));
        }
    }

    /**
     * 클라이언트에게 메시지 전송
     */
    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.serialize());
        }
    }

    /**
     * 연결 종료 및 정리
     */
    private void cleanup() {
        if (currentRoomId != null) {
            handleRoomLeave();
        }

        server.removeClient(this);

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("🔒 클라이언트 연결 해제: " + nickname);
    }

    // Getters
    public String getNickname() {
        return nickname != null ? nickname : "Guest";
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public Integer getCurrentRoomId() {
        return currentRoomId;
    }
}
