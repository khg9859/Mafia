package client;

import protocol.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 게임 클라이언트 - 서버와 소켓 통신
 */
public class GameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread receiverThread;
    private boolean connected = false;

    private String username;
    private String nickname;
    private int userId;

    // 메시지 리스너들
    private List<MessageListener> listeners = new ArrayList<>();

    /**
     * 메시지 수신 리스너 인터페이스
     */
    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    /**
     * 서버에 연결
     */
    public boolean connect(String username, String nickname, int userId) {
        try {
            this.username = username;
            this.nickname = nickname;
            this.userId = userId;

            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            connected = true;

            System.out.println("✅ 서버 연결 성공: " + SERVER_HOST + ":" + SERVER_PORT);

            // 로그인 메시지 전송
            sendMessage(Message.login(username, nickname + "|" + userId));

            // 메시지 수신 스레드 시작
            startReceiverThread();

            return true;

        } catch (IOException e) {
            System.out.println("❌ 서버 연결 실패: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * 메시지 수신 스레드 시작
     */
    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            try {
                String line;
                while (connected && (line = in.readLine()) != null) {
                    Message msg = Message.deserialize(line);
                    if (msg != null) {
                        System.out.println("📩 서버로부터: " + msg);
                        notifyListeners(msg);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.out.println("⚠️ 서버 연결 끊김: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    /**
     * 서버에 메시지 전송
     */
    public void sendMessage(Message message) {
        if (out != null && connected) {
            out.println(message.serialize());
            System.out.println("📤 서버로 전송: " + message);
        } else {
            System.out.println("⚠️ 서버 미연결 상태 - 메시지 전송 실패");
        }
    }

    /**
     * 방 목록 요청
     */
    public void requestRoomList() {
        sendMessage(Message.roomListRequest());
    }

    /**
     * 방 입장 요청
     */
    public void joinRoom(int roomId) {
        sendMessage(Message.roomJoin(roomId));
    }

    /**
     * 방 퇴장
     */
    public void leaveRoom() {
        sendMessage(Message.roomLeave());
    }

    /**
     * 채팅 메시지 전송
     */
    public void sendChatMessage(String message) {
        sendMessage(new Message(Message.Type.CHAT_MESSAGE, message));
    }

    /**
     * 서버 연결 해제
     */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                sendMessage(new Message(Message.Type.DISCONNECT, ""));
                socket.close();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            System.out.println("🔒 서버 연결 해제");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 메시지 리스너 추가
     */
    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    /**
     * 메시지 리스너 제거
     */
    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * 모든 리스너에게 메시지 알림
     */
    private void notifyListeners(Message message) {
        for (MessageListener listener : listeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.out.println("⚠️ 리스너 처리 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Getters
    public boolean isConnected() {
        return connected;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }
}
